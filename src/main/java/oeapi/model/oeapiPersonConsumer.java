package oeapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.persistence.Convert;
import oeapi.converter.oeapiUnitaListEnrollmentConverter;

/**
 *
 * @author itziar.urrutia
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class oeapiPersonConsumer {

    private String consumerKey;
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
