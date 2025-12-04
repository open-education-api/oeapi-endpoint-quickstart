package oeapi.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import java.io.IOException;
import java.util.List;
import oeapi.model.oeapiLanguageTypedString;

import jakarta.persistence.Converter;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

@Converter
public class oeapiUnitaLanguageTypedStringListConverter implements AttributeConverter<List<List<oeapiLanguageTypedString>>, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<List<oeapiLanguageTypedString>> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert name list to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public List<List<oeapiLanguageTypedString>> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReferenceImpl());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to name list", e);
        }
    }

    private static class TypeReferenceImpl extends TypeReference<List<List<oeapiLanguageTypedString>>> {

        public TypeReferenceImpl() {
        }
    }
}
