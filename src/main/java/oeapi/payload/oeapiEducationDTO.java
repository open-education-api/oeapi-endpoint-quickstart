package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EntityListeners;

import oeapi.converter.oeapiResourcesConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.model.ModeOfDelivery;
import oeapi.model.Address;
import oeapi.model.Ext;
import oeapi.model.PrimaryCode;
import oeapi.model.oeapiLanguageTypedString;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.oeapiEducationListener;
import oeapi.validation.ValidItemYaml;
import oeapi.validation.ValidLanguageTypedString;

/**
 *
 * @author itziar.urrutia
 */
@EntityListeners(oeapiEducationListener.class)
public abstract class oeapiEducationDTO extends PrimaryCode {
//public abstract class oeapiEducationDTO extends ooapiDTO {

    @JsonProperty("otherCodes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("ext")
    @Column(columnDefinition = "text")
    private Ext ext;

    @JsonProperty("teachingLanguage")
    private String teachingLanguage;

    //Non-Mandatory fields
    @JsonProperty("enrollment")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true)
    private List<oeapiLanguageTypedString> enrollment;

    @JsonProperty("assessment")
    @ValidLanguageTypedString(isNull = true)
    private List<oeapiLanguageTypedString> assessment;

    //@JsonIgnore
    @JsonProperty("modeOfDelivery")
    private List<ModeOfDelivery> modeOfDelivery;
    //@JsonProperty("modeOfDelivery")
    //private List<String> modeOfDeliveryJSON;

    @Convert(converter = oeapiResourcesConverter.class)
    @JsonProperty("resources")
    private List<String> resources;

    @JsonProperty("addresses")
    @ValidItemYaml(yamlfile = "address.yml")
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

    public String getTeachingLanguage() {
        return teachingLanguage;
    }

    public void setTeachingLanguage(String teachingLanguage) {
        this.teachingLanguage = teachingLanguage;
    }

    public void setEnrollment(List<oeapiLanguageTypedString> enrollment) {

        this.enrollment = enrollment;
    }

    public List<oeapiLanguageTypedString> getEnrollment() {
        return enrollment;
    }

    public List<oeapiLanguageTypedString> getAssessment() {
        return assessment;
    }

    public void setAssessment(List<oeapiLanguageTypedString> assessment) {

        this.assessment = assessment;
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

}
