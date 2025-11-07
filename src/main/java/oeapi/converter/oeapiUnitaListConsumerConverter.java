package oeapi.converter;

import javax.persistence.Converter;
import oeapi.model.Consumer;

@Converter
public class oeapiUnitaListConsumerConverter extends oeapiListConverter<Consumer> {

    public oeapiUnitaListConsumerConverter() {
        super(Consumer.class);

    }
}

