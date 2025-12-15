package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import oeapi.converter.oeapiIdentifierEntryConverter;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.validation.ValidObjectYaml;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiDTO {

    @JsonProperty("primaryCode")
    @Convert(converter = oeapiIdentifierEntryConverter.class)
    @ValidObjectYaml(yamlfile = "identifierEntry.yaml")
    private oeapiIdentifierEntry primaryCode;

    /**
     * @return the primaryCode
     */
    public oeapiIdentifierEntry getPrimaryCode() {
        return primaryCode;
    }

    /**
     * @param primaryCode the primaryCode to set
     */
    public void setPrimaryCode(oeapiIdentifierEntry primaryCode) {
        /*
        ooapiUnitaSchemaValidator validator = new ooapiUnitaSchemaValidator("/identifierEntry.yml");
        try{
        validator.validate("", primaryCode);
    }
    catch (ValidationException e) {
            throw new IllegalArgumentException("Invalid primaryCode: " + e.getMessage(), e);
    }
         */
        this.primaryCode = primaryCode;

    }

}
