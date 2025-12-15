package oeapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidConsumer;
import oeapi.validation.ValidLanguageTypedString;
import oeapi.validation.ValidObjectYaml;

/**
 *
 * @author itziar
 */
@Entity(name = "education_specification")
public class EducationSpecification extends PrimaryCode {

    @Id
    @Column(name = "education_specification_id")
    @JsonProperty(value = "educationSpecificationId", access = JsonProperty.Access.WRITE_ONLY)
    private String educationSpecificationId = UUID.randomUUID().toString();

    @JsonProperty("otherCodes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    private String educationSpecificationType;  // To do Enum

    @JsonProperty("name")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> name;

    @JsonProperty("abbreviation")
    private String abbreviation;

    @JsonProperty("description")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true, message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> description;

    private String formalDocument;

    @JsonProperty(value = "level")
    private String levelType;

    private String sector;   // To do -> Enum
    private String levelOfQualificationType;  // To do -> Enum
    private String fieldOfStudy;

    @ValidObjectYaml(yamlfile = "studyLoad.yml")
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "studyload_id")
    private StudyLoad studyLoad;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> learningOutcomes;

    private String link;

    private EducationSpecification parent;

    // Children : EducationSpecification[] managed at service
    @JsonProperty("organization")
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = true)
    @JsonBackReference("educationSpecificationOrganization")
    private Organization organization;

    @JsonProperty("consumers")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    @ValidConsumer(yamlfile = "courseconsumer.yml")
    private List<Consumer> consumers;

    @JsonProperty("ext")
    @Column(columnDefinition = "text")
    private Ext ext;

    private LocalDate validFrom;
    private LocalDate validTo;

    // To do: TimelineOverrideEducationSpecification []
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
     * @return the levelType
     */
    public String getLevelType() {
        return levelType;
    }

    /**
     * @param levelType the levelType to set
     */
    public void setLevelType(String levelType) {
        this.levelType = levelType;
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
     * @return the learningOutcomes
     */
    public List<oeapiLanguageTypedString> getLearningOutcomes() {
        return learningOutcomes;
    }

    /**
     * @param learningOutcomes the learningOutcomes to set
     */
    public void setLearningOutcomes(List<oeapiLanguageTypedString> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
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
     * @return the validFrom
     */
    public LocalDate getValidFrom() {
        return validFrom;
    }

    /**
     * @param validFrom the validFrom to set
     */
    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * @return the validTo
     */
    public LocalDate getValidTo() {
        return validTo;
    }

    /**
     * @param validTo the validTo to set
     */
    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    /**
     * @return the educationSpecificationId
     */
    public String getEducationSpecificationId() {
        return educationSpecificationId;
    }

    /**
     * @param educationSpecificationId the educationSpecificationId to set
     */
    public void setEducationSpecificationId(String educationSpecificationId) {
        this.educationSpecificationId = educationSpecificationId;
    }

    /**
     * @return the educationSpecificationType
     */
    public String getEducationSpecificationType() {
        return educationSpecificationType;
    }

    /**
     * @param educationSpecificationType the educationSpecificationType to set
     */
    public void setEducationSpecificationType(String educationSpecificationType) {
        this.educationSpecificationType = educationSpecificationType;
    }

    /**
     * @return the formalDocument
     */
    public String getFormalDocument() {
        return formalDocument;
    }

    /**
     * @param formalDocument the formalDocument to set
     */
    public void setFormalDocument(String formalDocument) {
        this.formalDocument = formalDocument;
    }

    /**
     * @param level the level to set
     */
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
     * @return the levelOfQualificationType
     */
    public String getLevelOfQualificationType() {
        return levelOfQualificationType;
    }

    /**
     * @param levelOfQualificationType the levelOfQualificationType to set
     */
    public void setLevelOfQualificationType(String levelOfQualificationType) {
        this.levelOfQualificationType = levelOfQualificationType;
    }

    /**
     * @return the fieldOfStudy
     */
    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    /**
     * @param fieldOfStudy the fieldOfStudy to set
     */
    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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

    /**
     * @return the parent
     */
    public EducationSpecification getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(EducationSpecification parent) {
        this.parent = parent;
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

}
