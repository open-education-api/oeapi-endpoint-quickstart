package oeapi.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import oeapi.converter.oeapiExtConverter;
import oeapi.converter.oeapiResultConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.validation.ValidEnumYaml;

/**
 * OEAPI v6 association between a person and an offering.
 *
 * One table serves the course, programme and learning component offering
 * associations; the type follows from the offering it points to. It replaces
 * the v5 "association" entity, whose offering link was not a real relation.
 */
@Entity(name = "offering_association")
public class OfferingAssociation {

    @Id
    @Column(name = "association_id")
    private String associationId = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ValidEnumYaml(yamlfile = "associationRole.yml")
    private String role;

    @ValidEnumYaml(yamlfile = "state.yml")
    private String state;

    @ValidEnumYaml(yamlfile = "remoteState.yml")
    private String remoteState;

    private OffsetDateTime startDateTime;
    private OffsetDateTime expectedEndDateTime;
    private OffsetDateTime actualEndDateTime;

    // Course and programme offering associations carry a study load. StudyLoad is
    // an entity, so it is kept here as two plain columns instead of a relation.
    private String studyLoadUnit;
    private Float studyLoadValue;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiResultConverter.class)
    private Result result;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiExtConverter.class)
    private Ext ext;

    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public Offering getOffering() {
        return offering;
    }

    public void setOffering(Offering offering) {
        this.offering = offering;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
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

    public StudyLoad getStudyLoad() {
        if (studyLoadUnit == null && studyLoadValue == null) {
            return null;
        }
        StudyLoad studyLoad = new StudyLoad();
        studyLoad.setStudyLoadUnit(studyLoadUnit);
        studyLoad.setValue(studyLoadValue == null ? 0 : studyLoadValue);
        return studyLoad;
    }

    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoadUnit = studyLoad == null ? null : studyLoad.getStudyLoadUnit();
        this.studyLoadValue = studyLoad == null ? null : studyLoad.getValue();
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
