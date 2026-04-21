package oeapi.payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

/**
 * Mark DTO field as expandable.  This field should be declared public
 * and a setting should be declared.
 */
public @interface oeapiDTOExpandable { }
