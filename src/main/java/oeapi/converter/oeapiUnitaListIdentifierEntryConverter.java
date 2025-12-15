package oeapi.converter;

import jakarta.persistence.Converter;
import java.util.List;
import oeapi.model.oeapiIdentifierEntry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jakarta.persistence.AttributeConverter;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

@Converter
public class oeapiUnitaListIdentifierEntryConverter implements AttributeConverter<List<oeapiIdentifierEntry>, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<oeapiIdentifierEntry> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert map to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public List<oeapiIdentifierEntry> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReferenceImpl());
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert JSON to name list", e.getLocalizedMessage());
        }
    }

    private static class TypeReferenceImpl extends TypeReference<List<oeapiIdentifierEntry>> {

        public TypeReferenceImpl() {
        }
    }
}
