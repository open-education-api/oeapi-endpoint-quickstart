package oeapi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiConverter<T> implements AttributeConverter<T, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();
    
    private final Class<T> targetType;

    public oeapiConverter(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert attribute to JSON", e.getLocalizedMessage());
        }
    }

    @Override
    public T convertToEntityAttribute(String json) {
        if (json == null || json.equalsIgnoreCase("{}")
                || json.equalsIgnoreCase("[]") || json.equalsIgnoreCase("")) {
            return null;
        }

        try {
            return objectMapper.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert JSON to attribute", e.getLocalizedMessage());
        }
    }

}
