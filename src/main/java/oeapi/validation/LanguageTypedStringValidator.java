package oeapi.validation;

import java.util.List;
import java.util.Locale;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import oeapi.model.oeapiLanguageTypedString;
import oeapi.oeapiException;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
public class LanguageTypedStringValidator implements ConstraintValidator<ValidLanguageTypedString, List<oeapiLanguageTypedString>> {

    @Override
    public void initialize(ValidLanguageTypedString constraintAnnotation) {

        //ConstraintValidator.super.initialize(constraintAnnotation); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    public boolean isValid(List<oeapiLanguageTypedString> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Allow null, use @NotNull for non-null validation
        }

        for (oeapiLanguageTypedString map : value) {

            if (!isValidMap(map)) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to validate language: " + map.getLanguage());    
            }

            // Return false if any map fails validation
        }
        return true;
    }

    private boolean isValidMap(oeapiLanguageTypedString map) {
        if (map == null) {
            return false; // Map cannot be null
        }

        String value = map.getLanguage();

        // Example validation: ensure keys and values are non-null and non-empty
        // Example rule: keys must be "language" and values must match specific strings
        return isISOValidLanguage(value);
    }

    private boolean isISOValidLanguage(String value) {
        // Example: only allow certain language codes

        String[] parts = value.split("-");
        String language = parts[0]; // e.g., "es"

        // Validate the language code
        if (!isValidLanguage(language)) {
            return false;
        }

        // If a country code is provided, validate it
        if (parts.length == 2) {
            String country = parts[1]; // e.g., "ES"
            if (!isValidCountry(country)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidLanguage(String language) {

        // Check if the language is part of ISO 639-1
        for (String isoLang : Locale.getISOLanguages()) {

            if (isoLang.equals(language)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidCountry(String country) {
        // Check if the country is part of ISO 3166-1
        for (String isoCountry : Locale.getISOCountries()) {
            if (isoCountry.equals(country)) {
                return true;
            }
        }
        return false;
    }

}
