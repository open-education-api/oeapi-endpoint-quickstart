/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiUnitaListCostConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.model.AcademicSession;
import oeapi.model.Address;
import oeapi.model.ComponentOffering;
import oeapi.model.Consumer;
import oeapi.model.Cost;
import oeapi.model.CourseOffering;
import oeapi.model.Ext;
import oeapi.model.ModeOfDelivery;
import oeapi.model.Organization;
import oeapi.model.PrimaryCode;
import oeapi.model.ProgramOffering;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.oeapiLanguageTypedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author itziar.urrutia
 */
// To know how serialize it uses field offeringType
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "offeringType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CourseOffering.class, name = "course"),
    @JsonSubTypes.Type(value = ComponentOffering.class, name = "component"),
    @JsonSubTypes.Type(value = ProgramOffering.class, name = "program")
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DiscriminatorColumn(name = "offering_class", discriminatorType = DiscriminatorType.STRING)
@JsonPropertyOrder({"offeringId", "primaryCode", "offeringType", "name", "description", "teachingLanguage", "resultExpected", "modeOfDelivery"})


public class OfferingDTO extends PrimaryCode {

    /**
     * @return the academicSessionJSON
     */
    public String getAcademicSessionJSON() {
        return (academicSession == null) ? null : academicSession.getAcademicSessionId();
    }

    /**
     * @return the organizationJSON
     */
    public String getOrganizationJSON() {
        return (organization == null) ? null : organization.getOrganizationId();
    }

    /**
     * @param organizationJSON the organizationJSON to set
     */
    static Logger logger = LoggerFactory.getLogger(OfferingDTO.class);

    @JsonProperty(value = "offeringId")
    private String offeringId;

    @JsonProperty("otherCodes")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("ext")
    private Ext ext;

    //@JsonIgnore
    //private String startDateJSON;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")    
    private LocalDate startDate;

    //@JsonIgnore
    //private String endDateJSON;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String teachingLanguage;

    @JsonProperty("modeOfDelivery")
    private List<ModeOfDelivery> modeOfDelivery;

    private Integer maxNumberStudents;
    private Integer minNumberStudents;
    private Integer enrolledNumberStudents;
    private Integer pendingNumberStudents;
    private Boolean resultExpected;
    private String resultValueType;
    private String link;
    private LocalDate enrollStartDate;
    private LocalDate enrollEndDate;
    private LocalDate flexibleEntryPeriodStart;
    private LocalDate flexibleEntryPeriodEnd;

    // Si usa aqui (y esta bien para el JSON) no se puede usar como @DiscriminatorColumn
//    @JsonProperty("offeringType")
//    private String offeringType;
    @JsonProperty("addresses")
    private List<Address> addresses;

    @JsonProperty("priceInformation")
    @Convert(converter = oeapiUnitaListCostConverter.class)
    private List<Cost> priceInformation;

    @JsonProperty("consumers")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @JsonProperty("academicSession")
    private String academicSessionJSON;

    @JsonIgnore
    private AcademicSession academicSession;

    @JsonProperty("organization")
    private String organizationJSON;

    @JsonIgnore
    private Organization organization;

    @JsonProperty("abbreviation")
    private String abbreviation;

    @JsonProperty("name")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> name;

    @JsonProperty("description")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
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

    public OfferingDTO() {
    }

    // Mantain Id if supplied. Useful if only id is given
    @JsonCreator
    public OfferingDTO(String id) {
        this.offeringId = id;
    }

    /**
     * @return the offeringId
     */
    public String getOfferingId() {
        return offeringId;
    }

    /**
     * @param offeringId the offeringId to set
     */
    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
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
     * @return the maxNumberStudents
     */
    public Integer getMaxNumberStudents() {
        return maxNumberStudents;
    }

    /**
     * @param maxNumberStudents the maxNumberStudents to set
     */
    public void setMaxNumberStudents(Integer maxNumberStudents) {
        this.maxNumberStudents = maxNumberStudents;
    }

    /**
     * @return the minNumberStudents
     */
    public Integer getMinNumberStudents() {
        return minNumberStudents;
    }

    /**
     * @param minNumberStudents the minNumberStudents to set
     */
    public void setMinNumberStudents(Integer minNumberStudents) {
        this.minNumberStudents = minNumberStudents;
    }

    /**
     * @return the enrolledNumberStudents
     */
    public Integer getEnrolledNumberStudents() {
        return enrolledNumberStudents;
    }

    /**
     * @param enrolledNumberStudents the enrolledNumberStudents to set
     */
    public void setEnrolledNumberStudents(Integer enrolledNumberStudents) {
        this.enrolledNumberStudents = enrolledNumberStudents;
    }

    /**
     * @return the pendingNumberStudents
     */
    public Integer getPendingNumberStudents() {
        return pendingNumberStudents;
    }

    /**
     * @param pendingNumberStudents the pendingNumberStudents to set
     */
    public void setPendingNumberStudents(Integer pendingNumberStudents) {
        this.pendingNumberStudents = pendingNumberStudents;
    }

    /**
     * @return the resultExpected
     */
    public Boolean getResultExpected() {
        return resultExpected;
    }

    /**
     * @param resultExpected the resultExpected to set
     */
    public void setResultExpected(Boolean resultExpected) {
        this.resultExpected = resultExpected;
    }

    /**
     * @return the resultValueType
     */
    public String getResultValueType() {
        return resultValueType;
    }

    /**
     * @param resultValueType the resultValueType to set
     */
    public void setResultValueType(String resultValueType) {
        this.resultValueType = resultValueType;
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
     * @return the enrollStartDate
     */
    public LocalDate getEnrollStartDate() {
        return enrollStartDate;
    }

    /**
     * @param enrollStartDate the enrollStartDate to set
     */
    public void setEnrollStartDate(LocalDate enrollStartDate) {
        this.enrollStartDate = enrollStartDate;
    }

    /**
     * @return the enrollEndDate
     */
    public LocalDate getEnrollEndDate() {
        return enrollEndDate;
    }

    /**
     * @param enrollEndDate the enrollEndDate to set
     */
    public void setEnrollEndDate(LocalDate enrollEndDate) {
        this.enrollEndDate = enrollEndDate;
    }

    /**
     * @return the flexibleEntryPeriodStart
     */
    public LocalDate getFlexibleEntryPeriodStart() {
        return flexibleEntryPeriodStart;
    }

    /**
     * @param flexibleEntryPeriodStart the flexibleEntryPeriodStart to set
     */
    public void setFlexibleEntryPeriodStart(LocalDate flexibleEntryPeriodStart) {
        this.flexibleEntryPeriodStart = flexibleEntryPeriodStart;
    }

    /**
     * @return the flexibleEntryPeriodEnd
     */
    public LocalDate getFlexibleEntryPeriodEnd() {
        return flexibleEntryPeriodEnd;
    }

    /**
     * @param flexibleEntryPeriodEnd the flexibleEntryPeriodEnd to set
     */
    public void setFlexibleEntryPeriodEnd(LocalDate flexibleEntryPeriodEnd) {
        this.flexibleEntryPeriodEnd = flexibleEntryPeriodEnd;
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
     * @return the priceInformation
     */
    public List<Cost> getPriceInformation() {
        return priceInformation;
    }

    /**
     * @param priceInformation the priceInformation to set
     */
    public void setPriceInformation(List<Cost> priceInformation) {
        this.priceInformation = priceInformation;
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
     * @return the academicSession
     */
    public AcademicSession getAcademicSession() {
        return academicSession;
    }

    /**
     * @param academicSession the academicSession to set
     */
    public void setAcademicSession(AcademicSession academicSession) {
        this.academicSession = academicSession;
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

    // Since OfferingType cannot be declared as property as Jackson automatically creates it, set/get methods are special
    // Extended classes override this response for each type
    public String getOfferingType() {
        return ("none");
    }

    public void setOfferingType(String offeringType) {
        logger.warn("offeringType is determined automatically and cannot be set manually.");
    }


}
