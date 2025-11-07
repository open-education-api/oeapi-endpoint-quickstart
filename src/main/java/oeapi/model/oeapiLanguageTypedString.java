package oeapi.model;

/**
 *
 * @author itziar.urrutia
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import org.springframework.stereotype.Component;

@Component
public class oeapiLanguageTypedString implements Serializable {

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
