package oeapi.converter;

import jakarta.persistence.Converter;
import oeapi.model.Ext;

@Converter
public class oeapiExtConverter extends oeapiConverter<Ext> {
    public oeapiExtConverter() {
        super(Ext.class);
    }
}
