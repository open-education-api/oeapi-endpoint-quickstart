package oeapi.controller.requestparameters;

import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiOfferingRequestParam extends oeapiRequestParam {

    
    private static List<String> validSortAttributes = Arrays.asList("offeringId", "name", "startDate", "endDate");

    private String teachingLanguage;
    private String offeringType;
    private String resultExpected; // REVISAR ES BOOLEANO
    private String since;
    private String until;    
    

    public Map.Entry<String, String> getFilter() {
        if (getTeachingLanguage() != null) {
            return new AbstractMap.SimpleEntry<>("teachingLanguage", getTeachingLanguage());
        }
        if (getOfferingType() != null) {
            return new AbstractMap.SimpleEntry<>("offeringType", getOfferingType());
        }
        if (getResultExpected() != null) {
            return new AbstractMap.SimpleEntry<>("resultExpected", getResultExpected());
        }
        if (getSince() != null) {
            return new AbstractMap.SimpleEntry<>("startDate", getSince());
        }
        if (getUntil() != null) {
            return new AbstractMap.SimpleEntry<>("endDate", getUntil());
        }
        return null;
    }

    public Pageable toPageable() {
        return super.toPageable(getValidSortAttributes()); // Pass the valid sort attributes for Person
    }

    /**
     * @return the validSortAttributes
     */
    public static List<String> getValidSortAttributes() {
        return validSortAttributes;
    }

    /**
     * @param aValidSortAttributes the validSortAttributes to set
     */
    public static void setValidSortAttributes(List<String> aValidSortAttributes) {
        validSortAttributes = aValidSortAttributes;
    }

    /**
     * @return the teachingLanguage
     */
    public String getTeachingLanguage() {
        return teachingLanguage;
    }

    /**
     * @param teachingLanguage the teachingLanguage to set
     */
    public void setTeachingLanguage(String teachingLanguage) {
        this.teachingLanguage = teachingLanguage;
    }

    /**
     * @return the offeringType
     */
    public String getOfferingType() {
        return offeringType;
    }

    /**
     * @param offeringType the offeringType to set
     */
    public void setOfferingType(String offeringType) {
        this.offeringType = offeringType;
    }

    /**
     * @return the resultExpected
     */
    public String getResultExpected() {
        return resultExpected;
    }

    /**
     * @param resultExpected the resultExpected to set
     */
    public void setResultExpected(String resultExpected) {
        this.resultExpected = resultExpected;
    }

    /**
     * @return the since
     */
    public String getSince() {
        return since;
    }

    /**
     * @param since the since to set
     */
    public void setSince(String since) {
        this.since = since;
    }

    /**
     * @return the until
     */
    public String getUntil() {
        return until;
    }

    /**
     * @param until the until to set
     */
    public void setUntil(String until) {
        this.until = until;
    }

}
