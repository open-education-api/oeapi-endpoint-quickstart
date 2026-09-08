package oeapi.converter;

import jakarta.persistence.Converter;
import oeapi.model.Ext;

/**
 * Persists the free-form OOAPI {@code ext} object as JSON text.
 *
 * Without this converter Hibernate has no mapping for {@link Ext}; because Ext
 * implements Serializable it silently falls back to SerializableType and writes
 * a Java-serialized byte stream into the text column, which MySQL/MariaDB
 * rejects with:
 *
 *   Incorrect string value: '\xAC\xED\x00\x05sr...' for column 'ext' at row 1
 *
 * (0xACED0005 is the java.io.ObjectOutputStream stream header.) The failure only
 * shows up once a request actually carries a non-null "ext", because a null Ext
 * is written as SQL NULL.
 */
@Converter
public class oeapiExtConverter extends oeapiConverter<Ext> {

    public oeapiExtConverter() {
        super(Ext.class);
    }
}
