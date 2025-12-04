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
@Constraint(validatedBy = YamlObjectValidator.class)
public @interface ValidObjectYaml {

    public String yamlfile() default "default";

    public String type() default "";

    public String message() default "Must be valid JSON";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};

}
