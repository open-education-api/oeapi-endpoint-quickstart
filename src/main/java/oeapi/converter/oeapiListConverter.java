package oeapi.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
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

    // More tolerant view used ONLY when READING JSON back from the DB, so that
    // records written with an older/different structure (e.g. a new attribute
    // that did not exist yet, a removed field, or a single object where a list
    // is now expected) can still be retrieved instead of failing the request.
    //
    // An ObjectReader rather than a copied mapper: a Jackson 3 ObjectMapper is
    // immutable and copy() no longer exists. A reader IS the per-read view of the
    // shared mapper - it inherits its whole configuration, and these two feature
    // overrides apply here only, which is exactly what the copy was for.
    private final ObjectReader readReader = ooapiObjectMapper()
            .reader()
            .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

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
        } catch (JacksonException e) {
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
            // TypeFactory.defaultInstance() is gone in Jackson 3; the mapper's own
            // factory is the right one to ask anyway.
            JavaType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, targetType);

            return readReader.forType(listType).readValue(json);
        } catch (JacksonException e) {
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