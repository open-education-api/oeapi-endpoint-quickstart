package oeapi.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 *
 * @author itziar.urrutia
 */
@Constraint(validatedBy = AddressesValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAddresses {

    String message() default "Invalid addresses format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean isNull() default false;
}
