package oeapi.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import oeapi.converter.oeapiExtConverter;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiUnitaListIdentifierEntryConverter;
import oeapi.validation.ValidEnumYaml;

/**
 * OEAPI v6 LearningOutcome. Programmes, courses and learning components refer
 * to learning outcomes by id (learningOutcomeIds); outcomes form a hierarchy
 * through parentIds / childIds.
 */
@Entity(name = "learning_outcome")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"learningOutcomeId", "primaryCode", "name", "abbreviation", "description", "parentIds",
    "childIds", "fieldsOfStudy", "otherCodes", "complexityLevel", "validFrom", "validTo", "consumers", "ext"})
public class LearningOutcome extends PrimaryCode {

    @Id
    @Column(name = "learning_outcome_id")
    @JsonProperty("learningOutcomeId")
    private String learningOutcomeId = UUID.randomUUID().toString();

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> name;

    private String abbreviation;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> description;

    @ManyToMany
    @JoinTable(name = "learning_outcome_parent",
            joinColumns = @JoinColumn(name = "learning_outcome_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id"))
    @JsonIgnore
    private List<LearningOutcome> parents = new ArrayList<>();

    // Filled in by the service from the parent links of other outcomes
    @jakarta.persistence.Transient
    @JsonProperty("childIds")
    private List<String> childIds;

    @Column(length = 6)
    private String fieldsOfStudy;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListIdentifierEntryConverter.class)
    private List<oeapiIdentifierEntry> otherCodes;

    @ValidEnumYaml(yamlfile = "learningOutcomeLevel.yml")
    private String complexityLevel;

    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiExtConverter.class)
    private Ext ext;

    public LearningOutcome() {
    }

    public LearningOutcome(String learningOutcomeId) {
        this.learningOutcomeId = learningOutcomeId;
    }

    public String getLearningOutcomeId() {
        return learningOutcomeId;
    }

    public void setLearningOutcomeId(String learningOutcomeId) {
        this.learningOutcomeId = learningOutcomeId;
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

    @JsonIgnore
    public List<LearningOutcome> getParents() {
        return parents;
    }

    @JsonIgnore
    public void setParents(List<LearningOutcome> parents) {
        this.parents = parents;
    }

    @JsonProperty("parentIds")
    public List<String> getParentIds() {
        return parents == null ? new ArrayList<>()
                : parents.stream().map(LearningOutcome::getLearningOutcomeId).collect(Collectors.toList());
    }

    @JsonProperty("parentIds")
    public void setParentIds(List<String> parentIds) {
        this.parents = parentIds == null ? new ArrayList<>()
                : parentIds.stream().map(LearningOutcome::new).collect(Collectors.toList());
    }

    public List<String> getChildIds() {
        return childIds;
    }

    @JsonIgnore
    public void setChildIds(List<String> childIds) {
        this.childIds = childIds;
    }

    public String getFieldsOfStudy() {
        return fieldsOfStudy;
    }

    public void setFieldsOfStudy(String fieldsOfStudy) {
        this.fieldsOfStudy = fieldsOfStudy;
    }

    public List<oeapiIdentifierEntry> getOtherCodes() {
        return otherCodes;
    }

    public void setOtherCodes(List<oeapiIdentifierEntry> otherCodes) {
        this.otherCodes = otherCodes;
    }

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

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
}
