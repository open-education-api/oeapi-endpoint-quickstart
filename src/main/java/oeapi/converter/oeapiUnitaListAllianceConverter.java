package oeapi.converter;

import javax.persistence.Converter;
import oeapi.model.Alliance;


@Converter
public class oeapiUnitaListAllianceConverter extends oeapiListConverter<Alliance> {

    public oeapiUnitaListAllianceConverter() {
        super(Alliance.class);
    }
}
