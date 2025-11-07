package oeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import oeapi.converter.oeapiIdentifierEntryConverter;

import oeapi.validation.ValidObjectYaml;

/**
 *
 * @author itziar.urrutia
 */
@MappedSuperclass
public abstract class PrimaryCode implements Serializable {

    @JsonProperty("primaryCode")
    //@Embedded
    @Column(columnDefinition = "text", name = "primaryCode")
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
        this.primaryCode = primaryCode;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true; // Check if the objects are the same instance
        }
        if (o == null || getClass() != o.getClass()) {
            return false; // Check for null or different class
        }
        PrimaryCode that = (PrimaryCode) o;

        // Check equality of fields
        return Objects.equals(primaryCode, that.primaryCode);

    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryCode); // Generate a hash based on fields
    }

}
