package oeapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolationException;
import oeapi.oeapiException;
import oeapi.oeapiSchemaValidator;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
public class YamlEnumValidator implements ConstraintValidator<ValidEnumYaml, String> {

    private String yamlFile;

    @Override
    public void initialize(ValidEnumYaml constraintAnnotation) {

        this.yamlFile = constraintAnnotation.yamlfile();

    }

    @Override
    public boolean isValid(String t, ConstraintValidatorContext cvc) {

        if (t == null) {
            return true;
        }
        // OEAPI v6 enumerations are extensible: custom values must start with "x-"
        if (t.startsWith("x-") && t.length() > 2) {
            return true;
        }
        oeapiSchemaValidator validator = new oeapiSchemaValidator("/schemas/enum/" + this.yamlFile);
        try {
            validator.validate(t);
            return true;

        } catch (org.everit.json.schema.ValidationException ve) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Failed to validate enumerator: " + ve.getMessage());
        } catch (ConstraintViolationException e) {
            throw new oeapiException(HttpStatus.BAD_REQUEST, "Failed to validate [" + this.yamlFile + "] : " + e.getMessage());

        }
    }

}
