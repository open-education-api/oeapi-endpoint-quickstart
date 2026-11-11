package oeapi.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import oeapi.converter.oeapiExtConverter;
import oeapi.converter.oeapiUnitaListConsumerConverter;
import oeapi.validation.ValidEnumYaml;

/**
 * OEAPI v6 Membership of a person in a group. Replaces the v5 person_group
 * join table: a membership has its own state, role and period.
 */
@Entity(name = "membership")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "person_id"}))
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"personId", "groupId", "startDateTime", "endDateTime", "state", "role", "consumers", "ext"})
public class Membership {

    @Id
    @Column(name = "membership_id")
    @JsonIgnore
    private String membershipId = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private Group group;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    @JsonIgnore
    private Person person;

    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;

    @ValidEnumYaml(yamlfile = "membershipState.yml")
    private String state;

    @ValidEnumYaml(yamlfile = "membershipRole.yml")
    private String role;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaListConsumerConverter.class)
    private List<Consumer> consumers;

    @Column(columnDefinition = "text")
    @Convert(converter = oeapiExtConverter.class)
    private Ext ext;

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @JsonProperty("personId")
    public String getPersonId() {
        return person == null ? null : person.getPersonId();
    }

    @JsonProperty("groupId")
    public String getGroupId() {
        return group == null ? null : group.getGroupId();
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
