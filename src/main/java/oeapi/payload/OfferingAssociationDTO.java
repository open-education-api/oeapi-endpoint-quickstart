package oeapi.payload;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import oeapi.model.Consumer;
import oeapi.model.Ext;
import oeapi.model.Result;

/**
 * Properties shared by the OEAPI v6 offering associations (AssociationId and
 * AssociationProperties in the specification). Each subclass publishes the
 * offering under its own name: courseOfferingId, programmeOfferingId or
 * learningComponentOfferingId.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"associationId", "role", "state", "remoteState", "startDateTime",
    "expectedEndDateTime", "actualEndDateTime", "personId"})
public abstract class OfferingAssociationDTO {

    @JsonProperty("associationId")
    private String associationId;

    @JsonProperty("personId")
    private String personId;

    @JsonProperty("role")
    private String role;

    @JsonProperty("state")
    private String state;

    @JsonProperty("remoteState")
    private String remoteState;

    @JsonProperty("startDateTime")
    private OffsetDateTime startDateTime;

    @JsonProperty("expectedEndDateTime")
    private OffsetDateTime expectedEndDateTime;

    @JsonProperty("actualEndDateTime")
    private OffsetDateTime actualEndDateTime;

    @JsonProperty("result")
    private Result result;

    @JsonProperty("consumers")
    private List<Consumer> consumers;

    @JsonProperty("ext")
    private Ext ext;

    /** Id of the offering, published by the subclass under its v6 name. */
    @JsonIgnore
    private String offeringId;

    @JsonIgnore
    public String getOfferingId() {
        return offeringId;
    }

    @JsonIgnore
    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
    }

    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemoteState() {
        return remoteState;
    }

    public void setRemoteState(String remoteState) {
        this.remoteState = remoteState;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getExpectedEndDateTime() {
        return expectedEndDateTime;
    }

    public void setExpectedEndDateTime(OffsetDateTime expectedEndDateTime) {
        this.expectedEndDateTime = expectedEndDateTime;
    }

    public OffsetDateTime getActualEndDateTime() {
        return actualEndDateTime;
    }

    public void setActualEndDateTime(OffsetDateTime actualEndDateTime) {
        this.actualEndDateTime = actualEndDateTime;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
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
