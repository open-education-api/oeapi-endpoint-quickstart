package oeapi.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import oeapi.converter.oeapiExtConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidLanguageTypedString;
import oeapi.validation.ValidObjectYaml;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
@Entity(name = "ooapiGroup")
public class Group extends PrimaryCode {

    @Id
    @Column(name = "group_id")
    @JsonProperty("groupId")
    private String groupId = UUID.randomUUID().toString();

    @JsonIgnore
    private LocalDate startDate;
    @JsonIgnore
    private LocalDate endDate;
    private Integer personCount;

    @JsonProperty("otherCodes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("ext")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiExtConverter.class)
    private Ext ext;

    @oeapi.validation.ValidEnumYaml(yamlfile = "groupType.yml")
    private String groupType;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = true)
    @JsonIgnore
    private Organisation organisation;

    @ManyToOne
    @JoinColumn(name = "academic_session_id", nullable = true)
    @JsonIgnore
    private AcademicSession academicSession;

    @ManyToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER // fetches the child entities along with parent (not when required)
    )
    @JoinTable(
            name = "person_group",
            joinColumns = @JoinColumn(
                    name = "group_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "person_id"
            )
    )
    @JsonProperty("abbreviation")
    private String abbreviation;

    @JsonProperty("name")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> name;

    @JsonProperty("description")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true, message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> description;

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
     * @return the description
     */
    public List<oeapiLanguageTypedString> getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(List<oeapiLanguageTypedString> description) {
        this.description = description;
    }

    /**
     * @return the abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * @param abbreviation the abbreviation to set
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
     * @return the personCount
     */
    public Integer getPersonCount() {
        return personCount;
    }

    /**
     * @param personCount the personCount to set
     */
    public void setPersonCount(Integer personCount) {
        this.personCount = personCount;
    }

    /**
     * @return the extInfo
     */
//    public ooapiExtInfo getExtInfo() {
//        return extInfo;
//    }
    /**
     * @param extInfo the extInfo to set
     */
//    public void setExtInfo(ooapiExtInfo extInfo) {
//        this.extInfo = extInfo;
//    }
    /**
     * @return the groupType
     */
    public String getGroupType() {
        return groupType;
    }

    /**
     * @param groupType the groupType to set
     */
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    /**
     * @return the organisation
     */
    @JsonIgnore
    public Organisation getOrganisation() {
        return organisation;
    }

    /**
     * @param organisation the organisation to set
     */
    @JsonIgnore
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    /**
     * @return the otherCodes
     */
    public List<oeapiIdentifierEntry> getOtherCodes() {
        return otherCodes;
    }

    /**
     * @param otherCodes the otherCodes to set
     */
    public void setOtherCodes(List<oeapiIdentifierEntry> otherCodes) {
        this.otherCodes = otherCodes;
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


    public Group() {
    }

    public Group(String groupId) {
        this.groupId = groupId;
    }

    @JsonIgnore
    public AcademicSession getAcademicSession() {
        return academicSession;
    }

    @JsonIgnore
    public void setAcademicSession(AcademicSession academicSession) {
        this.academicSession = academicSession;
    }

    // ---- OEAPI v6 JSON view: date-times and ids, objects with ?expand ---------

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

    @JsonProperty("organisationId")
    public String getOrganisationId() {
        return organisation == null ? null : organisation.getOrganisationId();
    }

    @JsonProperty("organisationId")
    public void setOrganisationId(String organisationId) {
        this.organisation = organisationId == null ? null : new Organisation(organisationId);
    }

    @JsonProperty("academicSessionId")
    public String getAcademicSessionId() {
        return academicSession == null ? null : academicSession.getAcademicSessionId();
    }

    @JsonProperty("academicSessionId")
    public void setAcademicSessionId(String academicSessionId) {
        this.academicSession = academicSessionId == null ? null : new AcademicSession(academicSessionId);
    }
}
