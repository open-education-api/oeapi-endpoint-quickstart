package oeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.CascadeType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import oeapi.converter.oeapiResourcesConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringListConverter;
import oeapi.converter.oeapiUnitaListAddressConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidAddresses;
import oeapi.validation.ValidConsumer;
import oeapi.validation.ValidLanguageTypedString;

/**
 *
 * @author itziar.urrutia
 */
@MappedSuperclass

public abstract class oeapiEducation extends PrimaryCode {

    @JsonProperty("teachingLanguage")
    private String teachingLanguage;

    //Non-Mandatory fields
    @JsonProperty("enrollment")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true)
    private List<oeapiLanguageTypedString> enrollment;

    @JsonProperty("learningOutcomes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringListConverter.class)
    //@ValidLanguageTypedString(isNull = true)
    private List<List<oeapiLanguageTypedString>> learningOutcomes;

    @JsonProperty("assessment")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true)
    private List<oeapiLanguageTypedString> assessment;

    @JsonProperty("consumers")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    @ValidConsumer(yamlfile = "courseconsumer.yml")
    private List<Consumer> consumers;

    @JsonProperty("modeOfDelivery")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable
    private List<ModeOfDelivery> modeOfDelivery;

    @Convert(converter = oeapiResourcesConverter.class)
    @Column(columnDefinition = "text")
    @JsonProperty("resources")
    private List<String> resources;

    @JsonProperty("addresses")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListAddressConverter.class)
    @ValidAddresses(message = "Null or Invalid address string elements")
    private List<Address> addresses;

    @JsonProperty("validFrom")
    private LocalDate validFrom;

    @JsonProperty("validTo")
    private LocalDate validTo;

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

    @JsonProperty("otherCodes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("ext")
    @Column(columnDefinition = "text")
    private Ext ext;

    private String duration;

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
     * @return the enrollment
     */
    public List<oeapiLanguageTypedString> getEnrollment() {
        return enrollment;
    }

    /**
     * @param enrollment the enrollment to set
     */
    public void setEnrollment(List<oeapiLanguageTypedString> enrollment) {
        this.enrollment = enrollment;
    }

    /**
     * @return the learningOutcomes
     */
    public List<List<oeapiLanguageTypedString>> getLearningOutcomes() {
        return learningOutcomes;
    }

    /**
     * @param learningOutcomes the learningOutcomes to set
     */
    public void setLearningOutcomes(List<List<oeapiLanguageTypedString>> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    /**
     * @return the assessment
     */
    public List<oeapiLanguageTypedString> getAssessment() {
        return assessment;
    }

    /**
     * @param assessment the assessment to set
     */
    public void setAssessment(List<oeapiLanguageTypedString> assessment) {
        this.assessment = assessment;
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
     * @return the modeOfDelivery
     */
    public List<ModeOfDelivery> getModeOfDelivery() {
        return modeOfDelivery;
    }

    /**
     * @param modeOfDelivery the modeOfDelivery to set
     */
    public void setModeOfDelivery(List<ModeOfDelivery> modeOfDelivery) {
        this.modeOfDelivery = modeOfDelivery;
    }

    /**
     * @return the resources
     */
    public List<String> getResources() {
        return resources;
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    /**
     * @return the addresses
     */
    public List<Address> getAddresses() {
        return addresses;
    }

    /**
     * @param addresses the addresses to set
     */
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
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
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

}
