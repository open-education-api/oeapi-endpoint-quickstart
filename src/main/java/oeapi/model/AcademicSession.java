package oeapi.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import oeapi.converter.oeapiExtConverter;
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

    @JsonIgnore
    private LocalDate startDate;
    @JsonIgnore
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
    @Convert(converter = oeapiExtConverter.class)
    private Ext ext;

    @JsonIgnore
    private AcademicSession year;

    @JsonIgnore
    private AcademicSession parent;

    public AcademicSession() {
    }

    @JsonCreator
    public AcademicSession(String id) {
        this.academicSessionId = id;
    }

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
    @JsonIgnore
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    @JsonIgnore
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    @JsonIgnore
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    @JsonIgnore
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
    @JsonIgnore
    public AcademicSession getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    @JsonIgnore
    public void setParent(AcademicSession parent) {
        this.parent = parent;
    }

    /**
     * @return the year
     */
    @JsonIgnore
    public AcademicSession getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    @JsonIgnore
    public void setYear(AcademicSession year) {
        this.year = year;
    }


    // ---- OEAPI v6 JSON view --------------------------------------------------
    // Dates are published as date-times (midnight UTC); parent and year as ids,
    // the full objects being available with ?expand=parent,year

    @JsonProperty("startDateTime")
    public OffsetDateTime getStartDateTime() {
        return startDate == null ? null : startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    @JsonProperty("startDateTime")
    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDate = startDateTime == null ? null : startDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
    }

    @JsonProperty("endDateTime")
    public OffsetDateTime getEndDateTime() {
        return endDate == null ? null : endDate.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    @JsonProperty("endDateTime")
    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDate = endDateTime == null ? null : endDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
    }

    @JsonProperty("parentId")
    public String getParentId() {
        return parent == null ? null : parent.getAcademicSessionId();
    }

    @JsonProperty("parentId")
    public void setParentId(String parentId) {
        this.parent = parentId == null ? null : new AcademicSession(parentId);
    }

    @JsonProperty("yearId")
    public String getYearId() {
        return year == null ? null : year.getAcademicSessionId();
    }

    @JsonProperty("yearId")
    public void setYearId(String yearId) {
        this.year = yearId == null ? null : new AcademicSession(yearId);
    }
}
