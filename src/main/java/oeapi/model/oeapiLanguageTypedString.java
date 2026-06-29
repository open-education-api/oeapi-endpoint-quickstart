package oeapi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.stereotype.Component;

/**
 *
 * @author itziar.urrutia
 */

@Component
public class oeapiLanguageTypedString extends oeapiEquals implements Serializable {

    @JsonProperty("language")
    private String language;
    @JsonProperty("value")
    private String value;

    /**
     * @return the language
     */
    public oeapiLanguageTypedString() {
    }

    public oeapiLanguageTypedString(String language, String value) {
        this.language = language;
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
