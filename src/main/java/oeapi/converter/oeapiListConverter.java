package oeapi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.List;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import oeapi.oeapiException;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */

@Converter
public class oeapiListConverter<T> implements AttributeConverter<List<T>, String> {

    private static final Logger logger = LoggerFactory.getLogger(oeapiListConverter.class);

    private final ObjectMapper objectMapper = ooapiObjectMapper();

    // More tolerant mapper used ONLY when READING JSON back from the DB, so that
    // records written with an older/different structure (e.g. a new attribute
    // that did not exist yet, a removed field, or a single object where a list
    // is now expected) can still be retrieved instead of failing the request.
    // Copied from the shared mapper so registered modules (JavaTime, etc.) are
    // preserved; feature changes here do NOT affect the shared singleton.
    private final ObjectMapper readMapper = ooapiObjectMapper().copy()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

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
        // Attribute not present / empty -> nothing to map.
        if (json == null || json.isBlank()
                || json.equalsIgnoreCase("{}")
                || json.equalsIgnoreCase("[]")) {
            return null;
        }

        try {
            JavaType listType = TypeFactory.defaultInstance()
                    .constructCollectionType(List.class, targetType);

            return readMapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            // When stored value uses an older or incompatible structure, rather
            // than throwing (which would break retrieval of the whole entity),
            // log it and treat this attribute as absent so legacy records keep
            // working. Re-saving the entity will rewrite the column in the
            // current format.
            logger.warn("Could not deserialize stored JSON for {} - treating attribute as empty. "
                    + "Reason: {}. Value: {}",
                    targetType.getSimpleName(), e.getOriginalMessage(), json);
            return null;
        }
    }

}