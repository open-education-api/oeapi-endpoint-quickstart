package oeapi.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

@Converter
public class oeapiUnitaListJSONConverter implements AttributeConverter<List<Map<String, String>>, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Map<String, String>> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert name list to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public List<Map<String, String>> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReferenceImpl());
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert JSON to name list", e.getLocalizedMessage());
        }
    }

    private static class TypeReferenceImpl extends TypeReference<List<Map<String, String>>> {

        public TypeReferenceImpl() {
        }
    }
}
