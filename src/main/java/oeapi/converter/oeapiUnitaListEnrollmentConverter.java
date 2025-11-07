package oeapi.converter;

import javax.persistence.Converter;

import oeapi.model.Enrollment;

@Converter
public class oeapiUnitaListEnrollmentConverter extends oeapiListConverter<Enrollment> {

    public oeapiUnitaListEnrollmentConverter() {
        super(Enrollment.class);
    }
}
