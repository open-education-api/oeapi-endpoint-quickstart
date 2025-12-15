package oeapi.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;

import oeapi.model.StudyLoad;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

@Converter
public class oeapiStudyLoadConverter implements AttributeConverter<StudyLoad, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Override
    public String convertToDatabaseColumn(StudyLoad attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert map to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public StudyLoad convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReferenceImpl());
        } catch (IOException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert JSON to name list", e.getLocalizedMessage());
        }
    }

    private static class TypeReferenceImpl extends TypeReference<StudyLoad> {

        public TypeReferenceImpl() {
        }
    }
}
