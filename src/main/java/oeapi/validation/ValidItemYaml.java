package oeapi.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = YamlItemValidator.class)
public @interface ValidItemYaml {

    public String yamlfile() default "default";

    public Class<? extends Object> targetType() default Object.class;

    public String message() default "Must be valid JSON";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};

}
