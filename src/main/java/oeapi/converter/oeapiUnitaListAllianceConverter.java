package oeapi.converter;

import jakarta.persistence.Converter;
import oeapi.model.Alliance;


@Converter
public class oeapiUnitaListAllianceConverter extends oeapiListConverter<Alliance> {

    public oeapiUnitaListAllianceConverter() {
        super(Alliance.class);
    }
}
