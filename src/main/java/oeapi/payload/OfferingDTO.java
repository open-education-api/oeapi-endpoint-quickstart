package oeapi.payload;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import oeapi.model.AcademicSession;
import oeapi.model.Address;
import oeapi.model.Consumer;
import oeapi.model.Cost;
import oeapi.model.Ext;
import oeapi.model.ModeOfDelivery;
import oeapi.model.Organisation;
import oeapi.model.PrimaryCode;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.oeapiLanguageTypedString;
import oeapi.validation.ValidEnumYaml;

/**
 * Properties shared by the four OEAPI v6 offering types (OfferingProperties in
 * the specification). There is no generic offering resource in v6: each
 * subclass publishes its own id (courseOfferingId, programmeOfferingId...).
 *
 * Property names follow the entity so ModelMapper can map them; the v6 JSON
 * names are given with {@code @JsonProperty} where they differ.
 *
 * @author itziar.urrutia
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"primaryCode", "name", "abbreviation", "description", "state", "rosteringState",
    "teachingLanguages", "modesOfDelivery", "startDateTime", "endDateTime", "enrolmentPeriods"})
public class OfferingDTO extends PrimaryCode {

    /** Internal id, published by the subclasses under their own v6 name. */
    @JsonIgnore
    private String offeringId;

    @JsonProperty("name")
    private List<oeapiLanguageTypedString> name;

    @JsonProperty("abbreviation")
    private String abbreviation;

    @JsonProperty("description")
    private List<oeapiLanguageTypedString> description;

    @JsonProperty("state")
    @ValidEnumYaml(yamlfile = "offeringState.yml")
    private String state;

    @JsonProperty("rosteringState")
    @ValidEnumYaml(yamlfile = "rosteringState.yml")
    private String rosteringState;

    @JsonProperty("teachingLanguages")
    private List<String> teachingLanguages;

    @JsonProperty("modesOfDelivery")
    private List<ModeOfDelivery> modesOfDelivery;

    @JsonProperty("startDateTime")
    private OffsetDateTime startDate;

    @JsonProperty("endDateTime")
    private OffsetDateTime endDate;

    // Enrolment period: the entity keeps one; v6 publishes a list (enrolmentPeriods)
    @JsonIgnore
    private OffsetDateTime enrollStartDate;

    @JsonIgnore
    private OffsetDateTime enrollEndDate;

    private Integer maxNumberStudents;
    private Integer enrolledNumberStudents;
    private Integer pendingNumberStudents;
    private Integer minNumberStudents;
    private Boolean resultExpected;

    @ValidEnumYaml(yamlfile = "resultValueType.yml")
    private String resultValueType;

    private String link;

    @JsonProperty("addresses")
    private List<Address> addresses;

    @JsonProperty("priceInformation")
    private List<Cost> priceInformation;

    @JsonProperty("otherCodes")
    private List<oeapiIdentifierEntry> otherCodes;

    @JsonProperty("consumers")
    private List<Consumer> consumers;

    @JsonProperty("ext")
    private Ext ext;

    // organisation

    @JsonProperty("organisationId")
    private String organisationId;

    @JsonIgnore
    @oeapiDTOExpandable
    public Organisation organisation;

    // academic session

    @JsonProperty("academicSessionId")
    private String academicSessionId;

    @JsonIgnore
    @oeapiDTOExpandable
    public AcademicSession academicSession;

    /** One element of enrolmentPeriods. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnrolmentPeriod {

        private OffsetDateTime startDateTime;
        private OffsetDateTime endDateTime;

        public EnrolmentPeriod() {
        }

        public EnrolmentPeriod(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }

        public OffsetDateTime getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(OffsetDateTime startDateTime) {
            this.startDateTime = startDateTime;
        }

        public OffsetDateTime getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(OffsetDateTime endDateTime) {
            this.endDateTime = endDateTime;
        }
    }

    @JsonProperty("enrolmentPeriods")
    public List<EnrolmentPeriod> enrolmentPeriodList() {
        List<EnrolmentPeriod> periods = new ArrayList<>();
        if (enrollStartDate != null) {
            periods.add(new EnrolmentPeriod(enrollStartDate, enrollEndDate));
        }
        return periods;
    }

    @JsonProperty("enrolmentPeriods")
    public void enrolmentPeriodsFromList(List<EnrolmentPeriod> periods) {
        if (periods == null || periods.isEmpty()) {
            this.enrollStartDate = null;
            this.enrollEndDate = null;
        } else {
            this.enrollStartDate = periods.get(0).getStartDateTime();
            this.enrollEndDate = periods.get(0).getEndDateTime();
        }
    }

    // ---- accessors ----------------------------------------------------------

    @JsonIgnore
    public String getOfferingId() {
        return offeringId;
    }

    @JsonIgnore
    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
    }

    public List<oeapiLanguageTypedString> getName() {
        return name;
    }

    public void setName(List<oeapiLanguageTypedString> name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public List<oeapiLanguageTypedString> getDescription() {
        return description;
    }

    public void setDescription(List<oeapiLanguageTypedString> description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRosteringState() {
        return rosteringState;
    }

    public void setRosteringState(String rosteringState) {
        this.rosteringState = rosteringState;
    }

    public List<String> getTeachingLanguages() {
        return teachingLanguages;
    }

    public void setTeachingLanguages(List<String> teachingLanguages) {
        this.teachingLanguages = teachingLanguages;
    }

    public List<ModeOfDelivery> getModesOfDelivery() {
        return modesOfDelivery;
    }

    public void setModesOfDelivery(List<ModeOfDelivery> modesOfDelivery) {
        this.modesOfDelivery = modesOfDelivery;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    @JsonIgnore
    public OffsetDateTime getEnrollStartDate() {
        return enrollStartDate;
    }

    @JsonIgnore
    public void setEnrollStartDate(OffsetDateTime enrollStartDate) {
        this.enrollStartDate = enrollStartDate;
    }

    @JsonIgnore
    public OffsetDateTime getEnrollEndDate() {
        return enrollEndDate;
    }

    @JsonIgnore
    public void setEnrollEndDate(OffsetDateTime enrollEndDate) {
        this.enrollEndDate = enrollEndDate;
    }

    public Integer getMaxNumberStudents() {
        return maxNumberStudents;
    }

    public void setMaxNumberStudents(Integer maxNumberStudents) {
        this.maxNumberStudents = maxNumberStudents;
    }

    public Integer getEnrolledNumberStudents() {
        return enrolledNumberStudents;
    }

    public void setEnrolledNumberStudents(Integer enrolledNumberStudents) {
        this.enrolledNumberStudents = enrolledNumberStudents;
    }

    public Integer getPendingNumberStudents() {
        return pendingNumberStudents;
    }

    public void setPendingNumberStudents(Integer pendingNumberStudents) {
        this.pendingNumberStudents = pendingNumberStudents;
    }

    public Integer getMinNumberStudents() {
        return minNumberStudents;
    }

    public void setMinNumberStudents(Integer minNumberStudents) {
        this.minNumberStudents = minNumberStudents;
    }

    public Boolean getResultExpected() {
        return resultExpected;
    }

    public void setResultExpected(Boolean resultExpected) {
        this.resultExpected = resultExpected;
    }

    public String getResultValueType() {
        return resultValueType;
    }

    public void setResultValueType(String resultValueType) {
        this.resultValueType = resultValueType;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Cost> getPriceInformation() {
        return priceInformation;
    }

    public void setPriceInformation(List<Cost> priceInformation) {
        this.priceInformation = priceInformation;
    }

    public List<oeapiIdentifierEntry> getOtherCodes() {
        return otherCodes;
    }

    public void setOtherCodes(List<oeapiIdentifierEntry> otherCodes) {
        this.otherCodes = otherCodes;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    public Ext getExt() {
        return ext;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }

    @JsonIgnore
    public Organisation getOrganisation() {
        return organisation;
    }

    @JsonIgnore
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        this.organisationId = organisation == null ? null : organisation.getOrganisationId();
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
        if (organisationId == null) {
            this.organisation = null;
        } else if (this.organisation == null || !organisationId.equals(this.organisation.getOrganisationId())) {
            this.organisation = new Organisation(organisationId);
        }
    }

    @JsonIgnore
    public AcademicSession getAcademicSession() {
        return academicSession;
    }

    @JsonIgnore
    public void setAcademicSession(AcademicSession academicSession) {
        this.academicSession = academicSession;
        this.academicSessionId = academicSession == null ? null : academicSession.getAcademicSessionId();
    }

    public String getAcademicSessionId() {
        return academicSessionId;
    }

    public void setAcademicSessionId(String academicSessionId) {
        this.academicSessionId = academicSessionId;
        if (academicSessionId == null) {
            this.academicSession = null;
        } else if (this.academicSession == null || !academicSessionId.equals(this.academicSession.getAcademicSessionId())) {
            this.academicSession = new AcademicSession(academicSessionId);
        }
    }
}
