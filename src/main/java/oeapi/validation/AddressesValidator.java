package oeapi.validation;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolationException;

import oeapi.model.Address;
import oeapi.oeapiException;
import oeapi.oeapiSchemaValidator;

import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpStatus;

/**
 *
 * @author itziar.urrutia
 */
public class AddressesValidator implements ConstraintValidator<ValidAddresses, List<Address>> {

    private final LanguageTypedStringValidator additionalValidator = new LanguageTypedStringValidator();

    @Override
    public void initialize(ValidAddresses constraintAnnotation) {

        //ConstraintValidator.super.initialize(constraintAnnotation); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    public boolean isValid(List<Address> value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }
        oeapiSchemaValidator validator = new oeapiSchemaValidator("/schemas/address.yml");
        for (Address map : value) {
            try {
                validator.validateAddress(map);
                additionalValidator.isValid(map.getAdditional(), context);

            } catch (ConstraintViolationException e) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to validate address: " + e.getMessage()
                );
            } catch (ValidationException ve) {
                throw new oeapiException(HttpStatus.NOT_FOUND, "Failed to validate address: " + ve.getMessage());
            }

        }

        return true;
    }
}
