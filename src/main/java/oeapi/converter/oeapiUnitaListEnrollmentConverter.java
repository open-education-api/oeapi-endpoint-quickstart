package oeapi.converter;

import jakarta.persistence.Converter;

import oeapi.model.Enrollment;

@Converter
public class oeapiUnitaListEnrollmentConverter extends oeapiListConverter<Enrollment> {

    public oeapiUnitaListEnrollmentConverter() {
        super(Enrollment.class);
    }
}
