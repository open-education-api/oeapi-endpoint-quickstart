package oeapi.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import oeapi.oeapiException;
import oeapi.oeapiSchemaValidator;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
public class YamlItemValidator implements ConstraintValidator<ValidItemYaml, List<?>> {

    private ObjectMapper objectMapper = ooapiObjectMapper();

    private String yamlFile;
    private Class<?> targetType;

    @Override
    public void initialize(ValidItemYaml constraintAnnotation) {

        this.yamlFile = constraintAnnotation.yamlfile();
        this.targetType = constraintAnnotation.targetType();
    }

    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext cvc) {

        if (value == null) {
            return true;
        }

        oeapiSchemaValidator validator = new oeapiSchemaValidator("/schemas/" + this.yamlFile);
        //offerings.forEach(offering
        for (Object item : value) {
            try {
                // Check if the item is of the expected targetType
                // Validate the item (assuming your validator works with individual objects)
                validator.validate(item);
            } catch (ConstraintViolationException ve) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to validate Language String: " + ve.getMessage());

            } catch (ValidationException ex) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to validate Language String: " + ex.getMessage()
                );
            } catch (Exception e) {

                //Add a validation error to the context for failed validation
                cvc.disableDefaultConstraintViolation();
                cvc.buildConstraintViolationWithTemplate("Failed to validate item: " + e.getMessage())
                        .addConstraintViolation();

                return false;

            }

        }
        return true;
    }
}
