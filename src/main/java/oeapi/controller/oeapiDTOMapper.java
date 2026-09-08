/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import oeapi.oeapiUtils;
import oeapi.payload.oeapiDTOExpandable;
import oeapi.service.oeapiEnumConversionService;
import oeapi.validation.ValidEnumYaml;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiDTOMapper<T, S> {
    static Logger logger = LoggerFactory.getLogger(oeapiDTOMapper.class);

    private final ModelMapper modelMapper = new ModelMapper();

    private List<String> enumFields;

    public ModelMapper getModelMapper() {
        return modelMapper;
    }
    private oeapiEnumConversionService enumService;
    private Class<S> dtoTargetType;

    private Class<T> objectTargetType;

    /**
     * @return the entity type this mapper maps FROM. Used by oeapiDTOMapperRegistry to key
     *         the mapper, so an expanded value of that type can be mapped to its DTO.
     */
    public Class<T> getObjectTargetType() {
        return objectTargetType;
    }

    /**
     * @return the DTO type this mapper maps TO.
     */
    public Class<S> getDtoTargetType() {
        return dtoTargetType;
    }

    public oeapiDTOMapper(Class<T> objectTargetType, Class<S> dtoTargetType, oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {

        this.dtoTargetType = dtoTargetType;
        this.objectTargetType = objectTargetType;
        this.enumService = ooapiEnumService;
        setEnumFields(enumFields);

        Converter<String, LocalDate> stringToLocalDate = new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(MappingContext<String, LocalDate> context) {
                return LocalDate.parse(context.getSource(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        };

        // Converter from LocalDate to String
        Converter<LocalDate, String> localDateToString = new Converter<LocalDate, String>() {
            @Override
            public String convert(MappingContext<LocalDate, String> context) {
                return context.getSource().format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        };

        // Add converters
        modelMapper.addConverter(stringToLocalDate);
        modelMapper.addConverter(localDateToString);

        // Resolve enum-backed fields during the mapping itself rather than afterwards.
        // ModelMapper reuses this TypeMap for nested properties of the same pair - an
        // Organization's parent and children, a Program's parent and children - so a post
        // converter fixes every DTO in the graph, while patching the result of map() only
        // ever fixed the root. That is what made ?expand=children answer with the raw
        // enumeration id ("organizationType": "5") instead of the value ("root").
        try {
            TypeMap<T, S> typeMap = modelMapper.getTypeMap(objectTargetType, dtoTargetType);

            if (typeMap == null) {
                typeMap = modelMapper.createTypeMap(objectTargetType, dtoTargetType);
            }

            typeMap.setPostConverter(context -> {
                applyEnumFixup(context.getSource(), context.getDestination());
                return context.getDestination();
            });

        } catch (RuntimeException ex) {
            // Never let mapper construction fail over this: toDTO() still fixes the root.
            logger.warn("Could not attach the enum post converter for {} -> {}: {}",
                    objectTargetType.getSimpleName(), dtoTargetType.getSimpleName(),
                    ex.getLocalizedMessage());
        }

    }

    public Long mapIdValue(String nameType, String valueType, oeapiEnumConversionService ooapiEnumService) {
        return ooapiEnumService.convertValueToId(nameType, valueType);
    }

    public Page<S> toPageDTO(Page<T> page) {

        List<S> dtoList = this.toDTOList(page.getContent());
        return new PageImpl<S>(dtoList, page.getPageable(), page.getTotalElements());
    }

    public List<S> toDTOList(List<T> items) {
        if (items == null) {
            return Arrays.asList();
        }
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Map.Entry<String, Long> mapValue(String methodName, String value, oeapiEnumConversionService ooapiEnumService) {
        if (enumFields.contains(methodName)) {
            Long id = mapIdValue(methodName, value, ooapiEnumService);
            return new AbstractMap.SimpleEntry<>(methodName + "Id", id);
        }
        return null;
    }

    public T toEntity(S dto) {

        if (dto == null) {
            return null;
        }
        T entity = getModelMapper().map(dto, objectTargetType);
        try {
            for (String fieldName : this.enumFields) {
                Field fieldDTO = dtoTargetType.getDeclaredField(fieldName);
                String stringValue = null;
                fieldDTO.setAccessible(true);
                Object value = fieldDTO.get(dto);
                if (value != null) {
                    stringValue = value.toString();
                }

                Long id = getEnumService().convertValueToId(fieldName, stringValue);
                String fieldNameId = fieldName + "Id";
                Field fieldEntity = objectTargetType.getDeclaredField(fieldNameId);
                fieldEntity.setAccessible(true);
                fieldEntity.set(entity, id);
            }
        } catch (NoSuchFieldException | IllegalAccessException err) {

        } finally {
            return entity;

        }
    }

    public S toDTO(T e) {

        if (e == null) {
            return null;
        }
        S dto = getModelMapper().map(e, dtoTargetType);

        // Belt and braces: the TypeMap post converter normally does this during the mapping
        // itself (including for nested DTOs). Repeating it is harmless - the value is always
        // derived from the entity's id, never from what is currently on the DTO - and keeps
        // the root correct even if the post converter could not be attached.
        applyEnumFixup(e, dto);

        return dto;
    }

    /**
     * Replaces enum-backed fields on a mapped DTO with their human readable value.
     *
     * The entity keeps these as "&lt;field&gt;Id", a Long pointing into the enumeration
     * table, while the DTO exposes "&lt;field&gt;" as the value. ModelMapper token matches
     * "&lt;field&gt;Id" onto "&lt;field&gt;" and leaves the raw id sitting there, so it has
     * to be replaced. Idempotent, and a no-op for a pair that has no such field.
     */
    private void applyEnumFixup(Object source, Object destination) {

        if (source == null || destination == null || enumFields == null || getEnumService() == null) {
            return;
        }

        for (String fieldName : enumFields) {
            try {
                // Read through the getter when there is one: the source may be a lazy
                // Hibernate proxy, whose own fields are empty until it is initialized.
                Object rawId = readProperty(source, fieldName + "Id");

                Field valueField = findField(destination.getClass(), fieldName);

                if (valueField == null) {
                    continue;   // this DTO does not carry that enum field
                }

                // Only ever REPLACE a value we can actually resolve. Never clear one:
                // some entities keep a field the service lists as an enum as a plain
                // String with no "<field>Id" at all (Program.level maps to the
                // program_level column, while Course.levelId is a real enumeration id).
                // For those the value ModelMapper mapped is already correct, and
                // overwriting it with null loses it.
                if (rawId instanceof Long) {
                    String resolved = getEnumService().convertIdToValue((Long) rawId);

                    if (resolved != null) {
                        valueField.set(destination, resolved);
                    }
                }

            } catch (IllegalAccessException | RuntimeException ex) {
                logger.debug("Could not resolve enum field {} on {}: {}", fieldName,
                        destination.getClass().getSimpleName(), ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Reads a property by getter, falling back to the declared field. Returns null when
     * neither exists, which is how a pair without that enum field is detected.
     */
    private static Object readProperty(Object target, String property) throws IllegalAccessException {

        String getterName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);

        try {
            Method getter = target.getClass().getMethod(getterName);
            return getter.invoke(target);
        } catch (NoSuchMethodException | java.lang.reflect.InvocationTargetException ex) {
            Field field = findField(target.getClass(), property);
            return (field == null) ? null : field.get(target);
        }
    }

    /** Finds an accessible declared field anywhere up the hierarchy, or null. */
    private static Field findField(Class<?> type, String name) {

        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // keep walking up
            }
        }
        return null;
    }

    /**
     * @param enumFields the enumFields to set
     */
    public void setEnumFields(List<String> enumFields) {
        this.enumFields = mergeEnumFields(enumFields, dtoTargetType);
    }

    /**
     * Enum fields are declared in two places today: the list each service hands to
     * initializeMapper(), and @ValidEnumYaml on the DTO field itself. The two had already
     * drifted - CourseDTO.sector carries the annotation but never appeared in the list - so
     * take the union. The annotation alone is then enough to have a field resolved, and any
     * list already in use keeps working.
     */
    private static List<String> mergeEnumFields(List<String> declared, Class<?> dtoType) {

        Set<String> merged = new LinkedHashSet<>();

        if (declared != null) {
            merged.addAll(declared);
        }

        for (Class<?> current = dtoType; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(ValidEnumYaml.class)) {
                    merged.add(field.getName());
                }
            }
        }

        return new ArrayList<>(merged);
    }

    /**
     * @return the enumService
     */
    public oeapiEnumConversionService getEnumService() {
        return enumService;
    }

    /**
     * @param enumService the enumService to set
     */
    public void setEnumService(oeapiEnumConversionService enumService) {
        this.enumService = enumService;
    }

    private ObjectMapper objectMapper = oeapiUtils.ooapiObjectMapper();

    public String toJSON(T e, String expand) throws JsonProcessingException {
        S dto = this.toDTO(e);

        ObjectNode node = objectMapper.valueToTree(dto);

        // promote requested expansions
        if (expand != null && !expand.isEmpty()) {
            String[] expandFields = expand.split(",");
            for (String fieldName : expandFields) {
                try {
                    Field field = dto.getClass().getField(fieldName);
                    if (field.isAnnotationPresent(oeapiDTOExpandable.class)) {
                        // Expandable fields are declared with the ENTITY type, so serializing
                        // the value as it stands answers with the entity's shape (the raw
                        // "<field>Id" columns and all). Map it through the DTO mapper
                        // registered for its type first; a type with no registered mapper is
                        // returned unchanged, as is a value that is already a DTO.
                        Object value = oeapiDTOMapperRegistry.toDTOGraph(field.get(dto));
                        node.set(fieldName, objectMapper.valueToTree(value));
                    } else {
                        logger.warn("Non-expandable field requested: {}#{}",
                                    dto.getClass().getCanonicalName(), fieldName);
                    }
                } catch (NoSuchFieldException ex) {
                    logger.error("Expandable field not found or not public: {}#{}",
                                 dto.getClass().getCanonicalName(), fieldName);
                } catch (IllegalAccessException ex) {
                    logger.error("Expandable field not accessible {}#{}",
                                 dto.getClass().getCanonicalName(), fieldName);
                }
            }
        }

        return objectMapper.writeValueAsString(node);
    }
}
