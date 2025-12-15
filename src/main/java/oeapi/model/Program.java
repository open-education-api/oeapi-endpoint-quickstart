package oeapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import java.util.List;

import java.util.UUID;
import jakarta.persistence.CascadeType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.validation.ValidEnumYaml;
import oeapi.validation.ValidLanguageTypedString;
import oeapi.validation.ValidObjectYaml;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@JsonPropertyOrder({"programId", "primaryCode", "name", "abbreviation", "description", "programType", "teachingLanguage", "level"})
public class Program extends oeapiEducation {

    @Id
    @Column(name = "program_id")
    private String programId = UUID.randomUUID().toString();

    @JsonProperty("programType")
    //@EnumType(name = "programEnum")
    @ValidEnumYaml(yamlfile = "programType.yml")
    private String programType;

    @ValidEnumYaml(yamlfile = "levelType.yml")
    @Column(name = "program_level")
    private String level;

    @ValidEnumYaml(yamlfile = "levelOfQualificationType.yml")
    private String levelOfQualification;

    @ValidEnumYaml(yamlfile = "qualificationAwardedType.yml")
    private String qualificationAwarded;

    private String fieldsOfStudyId;

    @ValidEnumYaml(yamlfile = "sectorType.yml")
    private String sector;

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

    @JsonProperty("coordinators")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable
    private List<Person> coordinators;

    @JsonProperty("educationSpecification")
    @ManyToOne
    @JoinColumn(name = "education_specification_id", nullable = true)
    //@JsonBackReference
    private EducationSpecification educationSpecification;

    @JsonProperty("organization")
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = true)
    @JsonBackReference("programOrganization")
    private Organization organization;

    public Program() {
    }

    @JsonCreator
    public Program(String id) {
        this.programId = id;
    }

    /**
     * @return the programId
     */
    public String getProgramId() {
        return programId;
    }

    /**
     * @param programId the programId to set
     */
    public void setProgramId(String programId) {
        this.programId = programId;
    }

    /**
     * @return the programType
     */
    public String getProgramType() {
        return programType;
    }

    /**
     * @param programType the programType to set
     */
    public void setProgramType(String programType) {
        this.programType = programType;
    }

    /**
     * @return the educationSpecification
     */
    public EducationSpecification getEducationSpecification() {
        return educationSpecification;
    }

    /**
     * @param educationSpecification the educationSpecification to set
     */
    public void setEducationSpecification(EducationSpecification educationSpecification) {
        this.educationSpecification = educationSpecification;
    }

    /**
     * @return the organization
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
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
     * @return the sector
     */
    public String getSector() {
        return sector;
    }

    /**
     * @param sector the sector to set
     */
    public void setSector(String sector) {
        this.sector = sector;
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

}
