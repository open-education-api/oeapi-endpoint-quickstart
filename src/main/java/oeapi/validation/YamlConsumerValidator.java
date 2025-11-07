package oeapi.validation;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolationException;

import oeapi.model.Consumer;
import oeapi.oeapiException;
import oeapi.oeapiSchemaValidator;

import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
public class YamlConsumerValidator implements ConstraintValidator<ValidConsumer, List<Consumer>> {

    private String yamlFile;

    @Override
    public void initialize(ValidConsumer constraintAnnotation) {

        this.yamlFile = constraintAnnotation.yamlfile();
    }

    @Override
    public boolean isValid(List<Consumer> value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }
        oeapiSchemaValidator validator = new oeapiSchemaValidator("/schemas/" + this.yamlFile);
        for (Consumer consumer : value) {
            try {
                validator.validate(consumer);
            } catch (ConstraintViolationException e) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to validate consumer [" + this.yamlFile + "] :" + e.getMessage());    
            } catch (ValidationException ve) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to validate consumer [" + this.yamlFile + "] :" + ve.getMessage());
            }

        }

        return true;
    }
}
