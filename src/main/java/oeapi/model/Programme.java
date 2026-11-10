package oeapi.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.validation.ValidEnumYaml;
import oeapi.validation.ValidLanguageTypedString;
import oeapi.validation.ValidObjectYaml;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "programme")
public class Programme extends oeapiEducation {

    @Id
    @Column(name = "programme_id", updatable = false, nullable = false)
    private String programmeId = UUID.randomUUID().toString();

    @JsonProperty("programmeType")
    @ValidEnumYaml(yamlfile = "programmeType.yml")
    private String programmeType;

    @ValidEnumYaml(yamlfile = "levelType.yml")
    @Column(name = "programme_level")
    private String level;

    @ValidEnumYaml(yamlfile = "levelOfQualificationType.yml")
    private String levelOfQualification;

    @ValidEnumYaml(yamlfile = "qualificationAwardedType.yml")
    private String qualificationAwarded;

    private String fieldsOfStudyId;

    private String link;

    private LocalDate firstStartDate;

    @JsonProperty("admissionRequirements")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true, message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> admissionRequirements;

    @JsonProperty("qualificationRequirements")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true, message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> qualificationRequirements;

    @JsonProperty("studyLoad")
    @ValidObjectYaml(yamlfile = "studyLoad.yaml")
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "studyload_id")
    private StudyLoad studyLoad;

    @JsonProperty("modeOfStudy")
    private String modeOfStudy;

    @JsonProperty("coordinators")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable
    private List<Person> coordinators;

    @JsonProperty("organisation")
    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = true)
    @JsonBackReference("programmeOrganisation")
    private Organisation organisation;

    public Programme() { }

    @JsonCreator
    public Programme(String id) {
        this.programmeId = id;
    }

    /**
     * @return the programmeId
     */
    public String getProgrammeId() {
        return programmeId;
    }

    /**
     * @param programmeId the programmeId to set
     */
    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    /**
     * @return the programmeType
     */
    public String getProgrammeType() {
        return programmeType;
    }

    /**
     * @param programmeType the programmeType to set
     */
    public void setProgrammeType(String programmeType) {
        this.programmeType = programmeType;
    }

    /**
     * @return the organisation
     */
    public Organisation getOrganisation() {
        return organisation;
    }

    /**
     * @param organisation the organisation to set
     */
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the firstStartDate
     */
    public LocalDate getFirstStartDate() {
        return firstStartDate;
    }

    /**
     * @param firstStartDate the firstStartDate to set
     */
    public void setFirstStartDate(LocalDate firstStartDate) {
        this.firstStartDate = firstStartDate;
    }

    /**
     * @return the admissionRequirements
     */
    public List<oeapiLanguageTypedString> getAdmissionRequirements() {
        return admissionRequirements;
    }

    /**
     * @param admissionRequirements the admissionRequirements to set
     */
    public void setAdmissionRequirements(List<oeapiLanguageTypedString> admissionRequirements) {
        this.admissionRequirements = admissionRequirements;
    }

    /**
     * @return the qualificationRequirements
     */
    public List<oeapiLanguageTypedString> getQualificationRequirements() {
        return qualificationRequirements;
    }

    /**
     * @param qualificationRequirements the qualificationRequirements to set
     */
    public void setQualificationRequirements(List<oeapiLanguageTypedString> qualificationRequirements) {
        this.qualificationRequirements = qualificationRequirements;
    }

    /**
     * @return the studyLoad
     */
    public StudyLoad getStudyLoad() {
        return studyLoad;
    }

    /**
     * @param studyLoad the studyLoad to set
     */
    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoad = studyLoad;
    }

    public String getModeOfStudy() {
        return modeOfStudy;
    }

    public void setModeOfStudy(String modeOfStudy) {
        this.modeOfStudy = modeOfStudy;
    }

    /**
     * @return the coordinators
     */
    public List<Person> getCoordinators() {
        return coordinators;
    }

    /**
     * @param coordinators the coordinators to set
     */
    public void setCoordinators(List<Person> coordinators) {
        this.coordinators = coordinators;
    }

    /**
     * @return the levelOfQualification
     */
    public String getLevelOfQualification() {
        return levelOfQualification;
    }

    /**
     * @param levelOfQualification the levelOfQualification to set
     */
    public void setLevelOfQualification(String levelOfQualification) {
        this.levelOfQualification = levelOfQualification;
    }

    /**
     * @return the fieldsOfStudyId
     */
    public String getFieldsOfStudyId() {
        return fieldsOfStudyId;
    }

    /**
     * @param fieldsOfStudyId the fieldsOfStudyId to set
     */
    public void setFieldsOfStudyId(String fieldsOfStudyId) {
        this.fieldsOfStudyId = fieldsOfStudyId;
    }

    /**
     * @return the qualificationAwarded
     */
    public String getQualificationAwarded() {
        return qualificationAwarded;
    }

    /**
     * @param qualificationAwarded the qualificationAwarded to set
     */
    public void setQualificationAwarded(String qualificationAwarded) {
        this.qualificationAwarded = qualificationAwarded;
    }

    // parent and children

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private Programme parent;

    public Programme getParent() {
        return parent;
    }

    public void setParent(Programme parent) {
        this.parent = parent;
    }

    @JsonBackReference("programmeParent")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Programme> children;

    public List<Programme> getChildren() {
        return children;
    }

    public void setChildren(List<Programme> children) {
        this.children = children;
    }
}
