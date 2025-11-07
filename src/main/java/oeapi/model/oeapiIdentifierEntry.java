package oeapi.model;

import java.io.Serializable;
import java.util.Objects;
import oeapi.validation.ValidObjectYaml;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiIdentifierEntry implements Serializable {

    @ValidObjectYaml(yamlfile = "enum/codeType.yaml")
    private String codeType;

    private String code;

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public oeapiIdentifierEntry() {
    }

    public oeapiIdentifierEntry(String codeType, String code) {
        this.code = code;
        this.codeType = codeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true; // Check if the objects are the same instance
        }
        if (o == null || getClass() != o.getClass()) {
            return false; // Check for null or different class
        }
        oeapiIdentifierEntry that = (oeapiIdentifierEntry) o;

        // Check equality of fields
        return Objects.equals(codeType, that.codeType)
                && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeType, code); // Generate a hash based on fields
    }
}
