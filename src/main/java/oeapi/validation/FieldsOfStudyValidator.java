/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import oeapi.repository.oeapiFieldsOfStudyRepository;

/**
 *
 * @author itziar.urrutia
 */
@Component
public class FieldsOfStudyValidator implements ConstraintValidator<ValidFieldsOfStudy, String> {

    @Autowired
    private oeapiFieldsOfStudyRepository fosRepo;

    @Override
    public void initialize(ValidFieldsOfStudy constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String fieldsOfStudy, ConstraintValidatorContext context) {

        return fieldsOfStudy == null || fosRepo.existsByTxtEn(fieldsOfStudy);
    }

}
