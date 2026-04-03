package oeapi.payload;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.EntityListeners;
import oeapi.oeapiEducationListener;

import oeapi.model.Address;
import oeapi.model.Consumer;
import oeapi.model.Ext;
import oeapi.model.ModeOfDelivery;
import oeapi.model.Organization;
import oeapi.model.PrimaryCode;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.oeapiLanguageTypedString;

/**
 *
 * @author itziar.urrutia
 */
@EntityListeners(oeapiEducationListener.class)
public abstract class oeapiEducationDTO extends PrimaryCode {

    // name

    @JsonProperty("name")
    private List<oeapiLanguageTypedString> name;

    public List<oeapiLanguageTypedString> getName() {
        return name;
    }

    public void setName(List<oeapiLanguageTypedString> name) {
        this.name = name;
    }

    // description

    @JsonProperty("description")
    private List<oeapiLanguageTypedString> description;

    public List<oeapiLanguageTypedString> getDescription() {
        return description;
    }

    public void setDescription(List<oeapiLanguageTypedString> description) {
        this.description = description;
    }

    // abbreviation

    @JsonProperty("abbreviation")
    private String abbreviation;

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    // mode of delivery

    @JsonProperty("modeOfDelivery")
    private List<ModeOfDelivery> modeOfDelivery;

    public List<ModeOfDelivery> getModeOfDelivery() {
        return modeOfDelivery;
    }

    public void setModeOfDelivery(List<ModeOfDelivery> modeOfDelivery) {
        this.modeOfDelivery = modeOfDelivery;
    }

    // duration

    @JsonProperty("duration")
    private String duration;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    // teaching language

    @JsonProperty("teachingLanguage")
    private String teachingLanguage;

    public String getTeachingLanguage() {
        return teachingLanguage;
    }

    public void setTeachingLanguage(String teachingLanguage) {
        this.teachingLanguage = teachingLanguage;
    }

    // learning outcomes

    @JsonProperty("learningOutcomes")
    private List<List<oeapiLanguageTypedString>> learningOutcomes;

    public void setLearningOutcomes(List<List<oeapiLanguageTypedString>> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public List<List<oeapiLanguageTypedString>> getLearningOutcomes() {
        return learningOutcomes;
    }

    // enrollment

    @JsonProperty("enrollment")
    private List<oeapiLanguageTypedString> enrollment;

    public void setEnrollment(List<oeapiLanguageTypedString> enrollment) {
        this.enrollment = enrollment;
    }

    public List<oeapiLanguageTypedString> getEnrollment() {
        return enrollment;
    }

    // resources

    @JsonProperty("resources")
    private List<String> resources;

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    // assessment

    @JsonProperty("assessment")
    private List<oeapiLanguageTypedString> assessment;

    public List<oeapiLanguageTypedString> getAssessment() {
        return assessment;
    }

    public void setAssessment(List<oeapiLanguageTypedString> assessment) {
        this.assessment = assessment;
    }

    // addresses

    @JsonProperty("addresses")
    private List<Address> addresses;

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    // other codes

    @JsonProperty("otherCodes")
    private List<oeapiIdentifierEntry> otherCodes;

    public List<oeapiIdentifierEntry> getOtherCodes() {
        return otherCodes;
    }

    public void setOtherCodes(List<oeapiIdentifierEntry> otherCodes) {
        this.otherCodes = otherCodes;
    }

    // organization

    @JsonProperty("organization")
    private String organizationId;

    @JsonIgnore
    @oeapiDTOExpandable
    public Organization organization;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        this.organizationId = organization == null ? null : organization.getOrganizationId();
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        this.organization = organizationId == null ? null : new Organization(organizationId);
    }

    // consumers

    @JsonProperty("consumers")
    private List<Consumer> consumers;

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    // valid from to

    @JsonProperty("validFrom")
    private LocalDate validFrom;

    @JsonProperty("validTo")
    private LocalDate validTo;

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    // ext

    @JsonProperty("ext")
    private Ext ext;

    public Ext getExt() {
        return ext;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }
}
