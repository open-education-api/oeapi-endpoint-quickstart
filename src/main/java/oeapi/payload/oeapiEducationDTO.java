package oeapi.payload;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.EntityListeners;
import oeapi.oeapiEducationListener;
import oeapi.model.Address;
import oeapi.model.Consumer;
import oeapi.model.Ext;
import oeapi.model.ModeOfDelivery;
import oeapi.model.LearningOutcome;
import oeapi.model.Organisation;
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

    @JsonProperty("modesOfDelivery")
    private List<ModeOfDelivery> modesOfDelivery;

    public List<ModeOfDelivery> getModesOfDelivery() {
        return modesOfDelivery;
    }

    public void setModesOfDelivery(List<ModeOfDelivery> modesOfDelivery) {
        this.modesOfDelivery = modesOfDelivery;
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

    @JsonProperty("teachingLanguages")
    private List<String> teachingLanguages;

    public List<String> getTeachingLanguages() {
        return teachingLanguages;
    }

    public void setTeachingLanguages(List<String> teachingLanguages) {
        this.teachingLanguages = teachingLanguages;
    }

    // learning outcomes (OEAPI v6: learningOutcomeIds, objects with ?expand=learning_outcomes)

    @JsonIgnore
    @oeapiDTOExpandable
    public List<LearningOutcome> learningOutcomes;

    // v5 style inline texts received on input; turned into LearningOutcome resources on save
    @JsonIgnore
    private List<List<oeapiLanguageTypedString>> learningOutcomeTexts;

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

    @JsonProperty("learningOutcomeIds")
    public List<String> learningOutcomeIdList() {
        return learningOutcomes == null ? null
                : learningOutcomes.stream().map(LearningOutcome::getLearningOutcomeId).collect(java.util.stream.Collectors.toList());
    }

    @JsonProperty("learningOutcomeIds")
    public void learningOutcomeIdsFromList(List<String> ids) {
        this.learningOutcomes = ids == null ? null
                : ids.stream().map(LearningOutcome::new).collect(java.util.stream.Collectors.toList());
    }

    /** The outcome objects, only when the request asks for ?expand=learning_outcomes (lists included). */
    @JsonProperty("learningOutcomes")
    public List<LearningOutcome> learningOutcomesIfExpanded() {
        return oeapi.oeapiUtils.requestExpands().contains("learning_outcomes") ? learningOutcomes : null;
    }

    /**
     * Input of "learningOutcomes": v6 LearningOutcome objects (their ids are used) or,
     * for v5 style clients, a list of texts (each a list of language typed strings),
     * which become LearningOutcome resources when the item is saved.
     */
    @JsonProperty("learningOutcomes")
    public void learningOutcomesInput(tools.jackson.databind.JsonNode value) {
        if (value == null || !value.isArray() || value.isEmpty()) {
            return;
        }
        if (value.get(0).isArray()) {
            this.learningOutcomeTexts = oeapi.oeapiUtils.ooapiObjectMapper().convertValue(value,
                    new tools.jackson.core.type.TypeReference<List<List<oeapiLanguageTypedString>>>() { });
        } else if (this.learningOutcomes == null) {
            List<LearningOutcome> outcomes = new java.util.ArrayList<>();
            for (tools.jackson.databind.JsonNode item : value) {
                tools.jackson.databind.JsonNode id = item.get("learningOutcomeId");
                if (id != null && !id.isNull()) {
                    outcomes.add(new LearningOutcome(id.asString()));
                }
            }
            this.learningOutcomes = outcomes;
        }
    }

    // enrolment

    @JsonProperty("enrolment")
    private List<oeapiLanguageTypedString> enrolment;

    public void setEnrolment(List<oeapiLanguageTypedString> enrolment) {
        this.enrolment = enrolment;
    }

    public List<oeapiLanguageTypedString> getEnrolment() {
        return enrolment;
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

    // organisation

    @JsonProperty("organisationId")
    private String organisationId;

    @JsonIgnore
    @oeapiDTOExpandable
    public Organisation organisation;

    public Organisation getOrganisation() {
        return organisation;
    }

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
        } else if (this.organisation == null
                || !organisationId.equals(this.organisation.getOrganisationId())) {
            // Reference stub, so an id alone is enough on input. Never rebuild it over an
            // organisation already mapped for this id: on a read ModelMapper supplies the
            // real one through setOrganisation(), and replacing it would reduce
            // ?expand=organisation to a bare organisationId. The guard used "!=", which
            // compares String references rather than contents and was therefore always true.
            this.organisation = new Organisation(organisationId);
        }
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
    private OffsetDateTime validFrom;

    @JsonProperty("validTo")
    private OffsetDateTime validTo;

    public OffsetDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(OffsetDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public OffsetDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(OffsetDateTime validTo) {
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
