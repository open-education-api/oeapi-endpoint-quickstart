package oeapi.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import java.util.HashMap;
import java.util.Map;
import oeapi.converter.oeapiUnitaListAllianceConverter;
import oeapi.converter.oeapiUnitaListEnrollmentConverter;

/**
 *
 * @author Itziar
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class Consumer {

    private Map<String, Object> otherAttributes = new HashMap<>();
      
    private String consumerKey;

    // Allow any attributes, the ones not explicity specified go into the map

    @JsonAnySetter
    public void set(String name, Object value) {
        otherAttributes.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> get() {
        return otherAttributes;
    }


    @JsonProperty("alliances")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListAllianceConverter.class)
    private List<Alliance> alliances;

    @Convert(converter = oeapiUnitaListEnrollmentConverter.class)
    private List<Enrollment> enrollments;

    private String institutionBRINCode;

    /**
     * @return the consumerKey
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * @param consumerKey the consumerKey to set
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }



    /**
     * @return the alliances
     */
    public List<Alliance> getAlliances() {
        return alliances;
    }

    /**
     * @param alliances the alliances to set
     */
    public void setAlliances(List<Alliance> alliances) {
        this.alliances = alliances;
    }

    /**
     * @return the enrollments
     */
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    /**
     * @param enrollments the enrollments to set
     */
    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    /**
     * @return the institutionBRINCode
     */
    public String getInstitutionBRINCode() {
        return institutionBRINCode;
    }

    /**
     * @param institutionBRINCode the institutionBRINCode to set
     */
    public void setInstitutionBRINCode(String institutionBRINCode) {
        this.institutionBRINCode = institutionBRINCode;
    }

}
