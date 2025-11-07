package oeapi.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import oeapi.oeapiSchemaValidator;
import oeapi.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author itziar.urrutia
 */
public class YamlObjectValidator implements ConstraintValidator<ValidObjectYaml, Object> {

    Logger logger = LoggerFactory.getLogger(YamlObjectValidator.class);
    
    private String yamlFile;

    @Override
    public void initialize(ValidObjectYaml constraintAnnotation) {

        logger.debug("YamlObjectValidator constraintAnnotation: type=["+constraintAnnotation.type()+"] file: "+constraintAnnotation.yamlfile());
        
        this.yamlFile = ("".equals(constraintAnnotation.type())) ? constraintAnnotation.yamlfile() : constraintAnnotation.type() + "/" + constraintAnnotation.yamlfile();
    }

    @Override
    public boolean isValid(Object t, ConstraintValidatorContext cvc) {

        if (t == null) {
            return true;
        }
        oeapiSchemaValidator validator = new oeapiSchemaValidator("/schemas/" + this.yamlFile);
        try {
            validator.validate(t);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

}
