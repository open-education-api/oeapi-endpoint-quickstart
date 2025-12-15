package oeapi.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.Map;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

@Converter
public class oeapiUnitaJSONConverter implements AttributeConverter<Map<String, Object>, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert name list to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReferenceImpl());
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert JSON to name list", e.getLocalizedMessage());
        }
    }

    private static class TypeReferenceImpl extends TypeReference<Map<String, Object>> {

        public TypeReferenceImpl() {
        }
    }
}
