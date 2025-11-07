package oeapi.converter;

import javax.persistence.Converter;
import oeapi.model.Address;


@Converter
public class oeapiUnitaListAddressConverter extends oeapiListConverter<Address> {

    public oeapiUnitaListAddressConverter() {
        super(Address.class);
    }
}
