package oeapi.converter;

import oeapi.model.oeapiLanguageTypedString;
import javax.persistence.Converter;

@Converter
public class oeapiUnitaLanguageTypedStringConverter extends oeapiListConverter<oeapiLanguageTypedString> {

    public oeapiUnitaLanguageTypedStringConverter() {
        super(oeapiLanguageTypedString.class);
    }
}
