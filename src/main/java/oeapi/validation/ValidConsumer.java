package oeapi.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 *
 * @author itziar.urrutia
 */
@Constraint(validatedBy = YamlConsumerValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidConsumer {

    public String yamlfile() default "default";

    String message() default "Invalid addresses format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean isNull() default false;
}
