package oeapi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.List;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
@Converter
public class oeapiListConverter<T> implements AttributeConverter<List<T>, String> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    private final Class<T> targetType;

    public oeapiListConverter(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public String convertToDatabaseColumn(List<T> attribute) {
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
    public List<T> convertToEntityAttribute(String json) {
        if (json == null || json.equalsIgnoreCase("{}")
                || json.equalsIgnoreCase("[]") || json.equalsIgnoreCase("")) {
            return null;
        }

        try {
            JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, targetType);

            return objectMapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to convert JSON to attribute", e.getLocalizedMessage());
        }
    }

}
