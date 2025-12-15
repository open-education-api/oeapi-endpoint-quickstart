package oeapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import oeapi.converter.oeapiUnitaExtConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.converter.oeapiResultConverter;
import oeapi.validation.ValidObjectYaml;

/**
 *
 * @author itziar
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "association")
public class Association extends PrimaryCode {

    @Id
    @Column(name = "association_id")
    @JsonProperty(value = "associationId", access = JsonProperty.Access.WRITE_ONLY)
    private String associationId = UUID.randomUUID().toString();

// Relacion con OfferingOLD
//    @ManyToOne
//    @JoinColumn(name = "offering_id")
//    @JsonBackReference("associationOfferingReference")
    private Offering offering;

    @ManyToOne
    @JoinColumn(name = "person_id")
    @JsonBackReference("associationPesonReference")
    private Person person;

    private String associationType;

    private String state;

    private String remoteState;

    private String associationRole;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaExtConverter.class)
    private Ext ext;

    @Column(columnDefinition = "text", name = "result")
    @JsonProperty(value = "result")
    @Convert(converter = oeapiResultConverter.class)
    @ValidObjectYaml(yamlfile = "result.yaml")
    private Result result;

    private Organization issuer;

//    @ManyToOne
//    @JoinColumn(name = "organization_id", nullable = true)
//    //@JsonBackReference
//    private Organization organization;
    /**
     * @return the associationId
     */
    public String getAssociationId() {
        return associationId;
    }

    /**
     * @param associationId the associationId to set
     */
    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    /**
     * @return the offering
     */
    public Offering getOffering() {
        return offering;
    }

    /**
     * @param offering the offering to set
     */
    public void setOffering(Offering offering) {
        this.offering = offering;
    }

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * @return the associationType
     */
    public String getAssociationType() {
        return associationType;
    }

    /**
     * @param associationType the associationType to set
     */
    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the remoteState
     */
    public String getRemoteState() {
        return remoteState;
    }

    /**
     * @param remoteState the remoteState to set
     */
    public void setRemoteState(String remoteState) {
        this.remoteState = remoteState;
    }

    /**
     * @return the associationRole
     */
    public String getAssociationRole() {
        return associationRole;
    }

    /**
     * @param associationRole the associationRole to set
     */
    public void setAssociationRole(String associationRole) {
        this.associationRole = associationRole;
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
     * @return the result
     */
    public Result getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Result result) {
        this.result = result;
    }

}
