/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.controller;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import oeapi.service.oeapiEnumConversionService;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiDTOMapper<T, S> {

    private final ModelMapper modelMapper = new ModelMapper();

    private List<String> enumFields;

    public ModelMapper getModelMapper() {
        return modelMapper;
    }
    private oeapiEnumConversionService enumService;
    private Class<S> dtoTargetType;

    private Class<T> objectTargetType;

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
        try {
            for (String fieldName : this.enumFields) {
                String fieldNameId = fieldName + "Id";
                Long id = null;
                Field fieldEntity = objectTargetType.getDeclaredField(fieldNameId);
                fieldEntity.setAccessible(true);
                Object value = fieldEntity.get(e);
                if (value != null) {
                    id = (Long) value;
                }

                String stringValue = getEnumService().convertIdToValue(id);
                Field fieldDTO = dtoTargetType.getDeclaredField(fieldName);
                fieldDTO.setAccessible(true);
                fieldDTO.set(dto, stringValue);
            }
        } catch (NoSuchFieldException | IllegalAccessException err) {

        } finally {
            return dto;

        }
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

}
