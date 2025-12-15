package oeapi;

import java.net.URL;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class oeapiObjectsValidator implements Validator {

    Logger logger = LoggerFactory.getLogger(oeapiObjectsValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        // Add a specific class or interface that this Validator supports
        return Object.class.isAssignableFrom(clazz); // Replace Object with your target class or interface
    }

    @Override
    public void validate(Object target, Errors errors) {

        String className = target.getClass().getSimpleName().replace("DTO", "");
        
        // Needed because validator put the first one in lowercase, contrary to the class name.
        // So, yml files is better to nemed with stating lowercase as they are accessed internally by validation 
        className = className.substring(0, 1).toLowerCase() + className.substring(1);

        // Construct the path to the schema based on the class name       
        // First, check if schema exists... (Path is relative in this case)
        String schemaPath = "schemas/" + className + ".yml"; // relative to classpath
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = classLoader.getResource(schemaPath);        
        

        if (resourceUrl == null) {
            errors.reject("Failed JSON Validation", "Failed to find Schema yaml file at " + schemaPath);
        } else {
            
            // Initialize the custom schema validator
            oeapiSchemaValidator validator = new oeapiSchemaValidator("/"+schemaPath);

            try {
                validator.validate(target); // Perform validation
            } catch (ValidationException ex) {
                // Add validation error to the Errors object
                errors.reject("Failed JSON Validation", "Validation failed for " + className + ": " + ex.getLocalizedMessage());
            }
        }
    }

}
