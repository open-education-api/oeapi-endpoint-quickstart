package oeapi.controller;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import oeapi.service.oeapiDTOMapperService;
import oeapi.service.oeapiEnumConversionService;

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

    public Class<T> objectTargetType;

    public oeapiDTOMapper(Class<T> objectTargetType, Class<S> dtoTargetType, oeapiEnumConversionService ooapiEnumService, List<String> enumFields) {

        this.dtoTargetType = dtoTargetType;
        this.objectTargetType = objectTargetType;
        this.enumFields = enumFields;
        this.enumService = ooapiEnumService;

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

        // Resolve enum-backed fields during the mapping rather than afterwards. ModelMapper
        // reuses this TypeMap for nested properties of the same pair - an organization's
        // parent and children - so a post converter reaches every DTO in the graph, while
        // patching the result of map() only ever reached the root. That is what made
        // ?expand=children answer with the raw enumeration id instead of the value.
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
            // Never fail construction over this: toDTO() still fixes the root.
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

        // Belt and braces: the post converter normally does this during the mapping itself,
        // nested DTOs included. Repeating it is harmless - the value is always derived from
        // the entity's id, never from what is on the DTO - and keeps the root correct even if
        // the post converter could not be attached.
        applyEnumFixup(e, dto);

        return dto;
    }

    /**
     * Replaces enum-backed fields on a mapped DTO with their human readable value.
     *
     * The entity keeps these as "&lt;field&gt;Id", a Long pointing into the enumeration table,
     * while the DTO exposes "&lt;field&gt;" as the value. ModelMapper token matches
     * "&lt;field&gt;Id" onto "&lt;field&gt;" and leaves the raw id sitting there, so it has to
     * be replaced. Idempotent, and a no-op for a pair that has no such field.
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

                // Only ever REPLACE a value that resolves. Never clear one: some entities keep
                // a field the service lists as an enum as a plain String with no "<field>Id" at
                // all - Program.level maps to the program_level column, while Course.levelId is
                // a real enumeration id - and for those the mapped value is already correct.
                // The old code cleared them, and only got away with it because
                // "finally { return dto; }" swallowed the NoSuchFieldException.
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
     * Reads a property by getter, falling back to the declared field. Returns null when neither
     * exists, which is how a pair without that enum field is detected.
     */
    private static Object readProperty(Object target, String property) throws IllegalAccessException {

        String getterName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);

        try {
            Method getter = target.getClass().getMethod(getterName);
            return getter.invoke(target);
        } catch (NoSuchMethodException | InvocationTargetException ex) {
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
        this.enumFields = enumFields;
        if (enumFields == null) {
            this.enumFields = Arrays.asList();
        }

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

    public oeapiDTOMapperService mapperService;

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
                        Object value = getFieldValue(dto, fieldName);

                        // toExpandedValue also covers a collection of related objects, and
                        // falls back to the value itself when its type has no mapper, so the
                        // MapperNotFound case is handled there rather than here.
                        value = mapperService.toExpandedValue(value);
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

    private static Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Class<?> c = obj.getClass();

        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, c);
            Method getter = pd.getReadMethod();

            if (getter != null) return getter.invoke(obj);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
            // ignore
        }

        Field field = c.getField(fieldName);
        return field.get(obj);
    }
}
