package oeapi;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * OEAPI v6 date-time values on the wire.
 *
 * Jackson writes an OffsetDateTime with ISO_OFFSET_DATE_TIME, which drops the
 * seconds when they are zero ("2024-09-01T00:00Z"). RFC 3339 / JSON Schema
 * "date-time" require them, so values are written as 2024-09-01T00:00:00Z
 * (fractions of a second only when present).
 */
public final class OeapiDateTime {

    private static final DateTimeFormatter WHOLE_SECONDS = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXXX");

    private OeapiDateTime() {
    }

    public static String format(OffsetDateTime value) {
        return value.getNano() == 0
                ? WHOLE_SECONDS.format(value)
                : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value);
    }

    /** Module registered on the Spring JSON mapper and on oeapiUtils.ooapiObjectMapper(). */
    public static SimpleModule module() {
        SimpleModule module = new SimpleModule("oeapi-v6-date-time");
        module.addSerializer(OffsetDateTime.class, new ValueSerializer<OffsetDateTime>() {
            @Override
            public void serialize(OffsetDateTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
                gen.writeString(format(value));
            }
        });
        return module;
    }
}
