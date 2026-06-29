package oeapi.model;

import java.io.Serializable;
import oeapi.validation.ValidObjectYaml;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiIdentifierEntry extends oeapiEquals implements Serializable {

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
}
