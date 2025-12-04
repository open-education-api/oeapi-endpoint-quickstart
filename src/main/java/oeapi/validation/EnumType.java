package oeapi.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumTypeValidator.class)
public @interface EnumType {

    public String name() default "default";

    public String message() default "Must be a valid enum";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};

    public boolean isReadOnly() default false;

    boolean isNull() default false;

}
