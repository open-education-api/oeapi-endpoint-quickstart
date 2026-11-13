package oeapi.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import oeapi.converter.oeapiExtConverter;
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

    // OEAPI v6 teachingLanguages (RFC 5646), stored comma separated
    @Column(name = "teaching_languages")
    private String teachingLanguages;

    //Non-Mandatory fields
    @JsonProperty("enrolment")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true)
    private List<oeapiLanguageTypedString> enrolment;

    // OEAPI v6: learning outcomes are a resource of their own (/learning-outcomes).
    @ManyToMany
    @JsonIgnore
    private List<LearningOutcome> learningOutcomes;

    // v5 kept learning outcomes inline as texts. The column stays as an input area:
    // texts sent by v5 style clients (and the ones already stored) are turned into
    // LearningOutcome resources by LearningOutcomeService and the column is emptied.
    @JsonIgnore
    @Column(name = "learning_outcomes", columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringListConverter.class)
    private List<List<oeapiLanguageTypedString>> learningOutcomeTexts;

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

    @JsonProperty("modesOfDelivery")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(inverseJoinColumns = @JoinColumn(name = "mode_of_delivery_id"))
    private List<ModeOfDelivery> modesOfDelivery;

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
    @Convert(converter = oeapiExtConverter.class)
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

    @JsonProperty("teachingLanguages")
    public List<String> getTeachingLanguages() {
        return oeapi.oeapiUtils.splitCsv(teachingLanguages);
    }

    @JsonProperty("teachingLanguages")
    public void setTeachingLanguages(List<String> teachingLanguages) {
        this.teachingLanguages = oeapi.oeapiUtils.joinCsv(teachingLanguages);
    }

    /**
     * @return the enrolment
     */
    public List<oeapiLanguageTypedString> getEnrolment() {
        return enrolment;
    }

    /**
     * @param enrolment the enrolment to set
     */
    public void setEnrolment(List<oeapiLanguageTypedString> enrolment) {
        this.enrolment = enrolment;
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
    public List<ModeOfDelivery> getModesOfDelivery() {
        return modesOfDelivery;
    }

    /**
     * @param modeOfDelivery the modeOfDelivery to set
     */
    public void setModesOfDelivery(List<ModeOfDelivery> modesOfDelivery) {
        this.modesOfDelivery = modesOfDelivery;
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


    @JsonIgnore
    public List<LearningOutcome> getLearningOutcomes() {
        return learningOutcomes;
    }

    @JsonIgnore
    public void setLearningOutcomes(List<LearningOutcome> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    @JsonIgnore
    public List<List<oeapiLanguageTypedString>> getLearningOutcomeTexts() {
        return learningOutcomeTexts;
    }

    @JsonIgnore
    public void setLearningOutcomeTexts(List<List<oeapiLanguageTypedString>> learningOutcomeTexts) {
        this.learningOutcomeTexts = learningOutcomeTexts;
    }

    /** v6 learningOutcomeIds in the entity's own JSON (not a bean getter, ModelMapper ignores it). */
    @JsonProperty("learningOutcomeIds")
    public List<String> learningOutcomeIdList() {
        return learningOutcomes == null ? null
                : learningOutcomes.stream().map(LearningOutcome::getLearningOutcomeId).collect(java.util.stream.Collectors.toList());
    }
}
