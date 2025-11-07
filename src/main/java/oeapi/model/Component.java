package oeapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import oeapi.validation.ValidEnumYaml;

@Entity(name = "component")
//@JsonPropertyOrder({"componentId", "primaryCode", "name", "abbreviation", "componentType", "teachingLanguage"})
public class Component extends oeapiEducation {

    @Id
    @Column(name = "component_Id")
    private String componentId = UUID.randomUUID().toString();

    private Long componentTypeId;

    @JsonProperty("course")
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = true)
    @JsonBackReference("componentCourse")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;

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
     * @return the course
     */
    public Course getCourse() {
        return course;
    }

    /**
     * @param course the course to set
     */
    public void setCourse(Course course) {
        this.course = course;
    }

    /**
     * @return the componentId
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * @param componentId the componentId to set
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * @return the componentTypeId
     */
    public Long getComponentTypeId() {
        return componentTypeId;
    }

    /**
     * @param componentTypeId the componentTypeId to set
     */
    public void setComponentTypeId(Long componentTypeId) {
        this.componentTypeId = componentTypeId;
    }

    @Override
    public String toString() {
        String jsonCourse = "{}";

        ObjectWriter ow = ooapiObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            jsonCourse = ow.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(Component.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jsonCourse;
    }

}
