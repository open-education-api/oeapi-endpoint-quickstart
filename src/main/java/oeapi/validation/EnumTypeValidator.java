package oeapi.validation;

import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import oeapi.model.oeapiUnitaEnumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import oeapi.repository.oeapiUnitaEnumerationRepository;

/**
 *
 * @author itziar
 */
@Component
@Scope("request")
public class EnumTypeValidator implements ConstraintValidator<EnumType, String> {

    private String enumName;
    private boolean isReadOnly;

    @Autowired
    private oeapiUnitaEnumerationRepository repository;

    @Override
    public void initialize(EnumType constraintAnnotation) {
        this.isReadOnly = constraintAnnotation.isReadOnly();
        this.enumName = constraintAnnotation.name();
    }

    @Override
    public boolean isValid(String t, ConstraintValidatorContext cvc) {

        if (t == null) {
            return true;
        }

        Optional<oeapiUnitaEnumeration> e = repository.findEnumeratorByNameAndValue(this.enumName, t);
        if (e.isPresent()) {
            return true;
        }
        if (this.isReadOnly) {
            return false;
        } else {
            oeapiUnitaEnumeration newenum = new oeapiUnitaEnumeration();
            newenum.setName(this.enumName);
            newenum.setValue(t);
            newenum.setDescription("");
            repository.save(newenum);
            return true;
        }

    }
}
