package oeapi;

/**
 *
 * @author itziar.urrutia
 */
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;
import oeapi.model.Address;
import oeapi.model.StudyLoad;

import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;



//@Component
public class oeapiSchemaValidator {

    Logger logger = LoggerFactory.getLogger(oeapiSchemaValidator.class);

    private final Schema schema;

    public oeapiSchemaValidator(String file) throws ValidationException {

        // Load the YAML file
        logger.debug("\n\n ---- Validating Object using: "+file+ "\n");
        InputStream yamlSchemaStream = oeapiSchemaValidator.class.getResourceAsStream(file);
        
 
        Yaml yaml = new Yaml();
        Map<String, String> yamlSchemaMap = yaml.load(yamlSchemaStream);

         // Convert YAML schema to JSONObject
        JSONObject jsonSchemaObject = new JSONObject(yamlSchemaMap);

        // Load and validate the schema
        schema = SchemaLoader.load(jsonSchemaObject);

    }

    public void validateJSON(Map<String, String> jsonToValidate) throws ValidationException {
        logger.debug("\n\n ---- validateJSON...");
        JSONObject jsonObject = new JSONObject(jsonToValidate);
        Map<String, Object> jsonflatten = oeapiUtils.flattenJSONObject(jsonObject);
        jsonObject = new JSONObject(jsonflatten);
        schema.validate(jsonObject);

    }

    public void validate(Object jsonToValidate) throws ValidationException {
        logger.debug("\n\n ---- validate(Object jsonToValidate)...");
        JSONObject jsonObject = new JSONObject(jsonToValidate);
        Map<String, Object> jsonflatten = oeapiUtils.flattenJSONObject(jsonObject);
        jsonObject = new JSONObject(jsonflatten);

        try {
            schema.validate(jsonObject);
        } catch (ValidationException e) {
            throw e;
        }

    }

    public void validate(String jsonToValidate) throws ValidationException {
        logger.debug("\n\n ---- validate STRING...");
        //JSONObject jsonObject = new JSONObject(jsonToValidate);
        //Map<String, Object> jsonflatten = oeapiUtils.flattenJSONObject(jsonObject);
        //jsonObject = new JSONObject(jsonflatten);
        schema.validate(jsonToValidate);

    }

    public void validate(Map<String, Object> jsonToValidate) throws ValidationException {

        JSONObject jsonObject = new JSONObject(jsonToValidate);
        Map<String, Object> jsonflatten = oeapiUtils.flattenJSONObject(jsonObject);
        jsonObject = new JSONObject(jsonflatten);
        schema.validate(jsonObject);
    }

    public void validate(StudyLoad jsonToValidate) throws ValidationException {

        // Convert the Map to a JSONObject and validate
        logger.debug("\n*******\n Comprobando con validador...");
        JSONObject jsonObject = new JSONObject(jsonToValidate);
        schema.validate(jsonObject); // Throws ValidationException if invalid
    }

    public void validateAddress(Address jsonToValidate) throws ValidationException {

        // Convert the Map to a JSONObject and validate
        logger.debug("\n*******\n validateAddress...");
        JSONObject jsonObject = new JSONObject(jsonToValidate);
        try {
            schema.validate(jsonObject);
        } catch (ValidationException e) {
            throw e;
        }

    }

}
