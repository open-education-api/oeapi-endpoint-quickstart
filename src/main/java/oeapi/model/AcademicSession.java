package oeapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import oeapi.converter.oeapiUnitaExtConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.validation.ValidEnumYaml;

/**
 *
 * @author itziar
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "academic_session")
public class AcademicSession extends PrimaryCode {

    @Id
    @Column(name = "academic_session_id")
    @JsonProperty(value = "academicSessionId")
    private String academicSessionId = UUID.randomUUID().toString();

    private LocalDate startDate;
    private LocalDate endDate;

    @ValidEnumYaml(yamlfile = "academicSessionType.yml")
    private String academicSessionType;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> name;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaExtConverter.class)
    private Ext ext;

    @JsonProperty("year")
    private AcademicSession year;

    @JsonProperty("parent")
    private AcademicSession parent;

    // Children are managed at service from parent field, 
    // as it easier than managing a self reference(potentially recursive if wrong)    
    
    /**
     * @return the academicSessionId
     */
    public String getAcademicSessionId() {
        return academicSessionId;
    }

    /**
     * @param academicSessionId the academicSessionId to set
     */
    public void setAcademicSessionId(String academicSessionId) {
        this.academicSessionId = academicSessionId;
    }

    /**
     * @return the startDate
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the academicSessionType
     */
    public String getAcademicSessionType() {
        return academicSessionType;
    }

    /**
     * @param academicSessionType the academicSessionType to set
     */
    public void setAcademicSessionType(String academicSessionType) {
        this.academicSessionType = academicSessionType;
    }

    /**
     * @return the name
     */
    public List<oeapiLanguageTypedString> getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(List<oeapiLanguageTypedString> name) {
        this.name = name;
    }

    /**
     * @return the consumers
     */
    public List<Consumer> getConsumers() {
        return consumers;
    }

    /**
     * @param consumers the consumers to set
     */
    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    /**
     * @return the ext
     */
    public Ext getExt() {
        return ext;
    }

    /**
     * @param ext the ext to set
     */
    public void setExt(Ext ext) {
        this.ext = ext;
    }

    /**
     * @return the parent
     */
    public AcademicSession getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(AcademicSession parent) {
        this.parent = parent;
    }

    /**
     * @return the year
     */
    public AcademicSession getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(AcademicSession year) {
        this.year = year;
    }
    
}
