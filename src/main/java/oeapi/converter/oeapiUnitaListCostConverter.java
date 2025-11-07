/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.converter;


import javax.persistence.Converter;
import java.util.List;
import oeapi.model.Cost;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.persistence.AttributeConverter;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;


@Converter
public class oeapiUnitaListCostConverter implements AttributeConverter<List<Cost>, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Cost> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert map to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Cost> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReferenceImpl());
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert JSON to name list", e.getLocalizedMessage());
        }
    }

    private static class TypeReferenceImpl extends TypeReference<List<Cost>> {

        public TypeReferenceImpl() {
        }
    }
}
