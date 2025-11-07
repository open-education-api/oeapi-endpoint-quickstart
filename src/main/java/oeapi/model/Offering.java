package oeapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListAddressConverter;
import oeapi.converter.oeapiUnitaListCostConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidEnumYaml;
import oeapi.validation.ValidLanguageTypedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author itziar.urrutia
 */

// To know how serialize it uses field offeringType
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "offeringType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CourseOffering.class,    name = "course"),
    @JsonSubTypes.Type(value = ComponentOffering.class, name = "component"),    
    @JsonSubTypes.Type(value = ProgramOffering.class,   name = "program")
})
@Entity(name = "offering")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "offering_class", discriminatorType = DiscriminatorType.STRING)
@JsonPropertyOrder({"offeringId", "primaryCode", "offeringType", "name", "description", "teachingLanguage", "resultExpected", "modeOfDelivery"})
public class Offering extends PrimaryCode {

    static Logger logger = LoggerFactory.getLogger(Offering.class);    
    
    
    @Id
    @Column(name = "offering_id")
    @JsonProperty(value = "offeringId")
    private String offeringId = UUID.randomUUID().toString();

    @JsonProperty("otherCodes")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("ext")
    @Column(columnDefinition = "text")
    private Ext ext;

    private LocalDate startDate;
    private LocalDate endDate;
    private String teachingLanguage;

    @JsonProperty("modeOfDelivery")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable
    private List<ModeOfDelivery> modeOfDelivery;

    private Integer maxNumberStudents;
    private Integer minNumberStudents;
    private Integer enrolledNumberStudents;
    private Integer pendingNumberStudents;
    private Boolean resultExpected;

    @ValidEnumYaml(yamlfile = "resultValueType.yml")
    private String resultValueType;
    private String link;
    private LocalDate startEnrollDate;
    private LocalDate endEnrollDate;
    private LocalDate flexibleEntryPeriodStart;
    private LocalDate flexibleEntryPeriodEnd;

    // Si usa aqui (y esta bien para el JSON) no se puede usar como @DiscriminatorColumn
//    @JsonProperty("offeringType") 
//    private String offeringType;

    @JsonProperty("addresses")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListAddressConverter.class)
    private List<Address> addresses;

    @JsonProperty("priceInformation")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListCostConverter.class)
    private List<Cost> priceInformation;

    @JsonProperty("consumers")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @ManyToOne
    @JoinColumn(name = "academic_session_id", nullable = true)
    private AcademicSession academicSession;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;

    //@OneToMany(mappedBy = "offering")
    //@JsonManagedReference("associationOfferingReference")
    //private List<Association> associations;
    //@Column(name = "offering_type", insertable = false, updatable = false)
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

    public Offering() {
    }

    // Mantain Id if supplied. Useful if only id is given
    @JsonCreator
    public Offering(String id) {
        this.offeringId = id;
    }

    /**
     * @return the Id
     */
    public String getId() {
        return offeringId;
    }

    /**
     * @param Id the offeringId to set
     */
    public void setId(String offeringId) {
        this.offeringId = offeringId;
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
     * @return the startEnrollDate
     */
    public LocalDate getStartEnrollDate() {
        return startEnrollDate;
    }

    /**
     * @param startEnrollDate the startEnrollDate to set
     */
    public void setStartEnrollDate(LocalDate startEnrollDate) {
        this.startEnrollDate = startEnrollDate;
    }

    /**
     * @return the endEnrollDate
     */
    public LocalDate getEndEnrollDate() {
        return endEnrollDate;
    }

    /**
     * @param endEnrollDate the endEnrollDate to set
     */
    public void setEndEnrollDate(LocalDate endEnrollDate) {
        this.endEnrollDate = endEnrollDate;
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
        return("course");  // defaults to course if not detected from class
    }

    
    public void setOfferingType(String offeringType) {
        logger.warn("offeringType is determined automatically and cannot be set manually.");
    }    
    
    
}
