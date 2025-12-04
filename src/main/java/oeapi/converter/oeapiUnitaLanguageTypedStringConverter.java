package oeapi.converter;

import oeapi.model.oeapiLanguageTypedString;
import jakarta.persistence.Converter;

@Converter
public class oeapiUnitaLanguageTypedStringConverter extends oeapiListConverter<oeapiLanguageTypedString> {

    public oeapiUnitaLanguageTypedStringConverter() {
        super(oeapiLanguageTypedString.class);
    }
}
