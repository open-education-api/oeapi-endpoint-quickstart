package oeapi.payload;

import static oeapi.oeapiUtils.ooapiObjectMapper;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import oeapi.model.Course;
import oeapi.validation.ValidEnumYaml;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"componentId", "primaryCode", "name", "abbreviation", "componentType", "teachingLanguage"})
public class ComponentDTO extends oeapiEducationDTO {

    @JsonProperty("componentId")
    private String componentId = UUID.randomUUID().toString();

    @JsonProperty("componentType")
    @ValidEnumYaml(yamlfile = "componentType.yml")
    private String componentType;

    @JsonProperty("course")
    //@JsonIgnore
    private Course course;

    //@JsonProperty("course")
    @JsonIgnore
    private String courseJSON;

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
        this.courseJSON = "";
        if (course != null) {
            this.courseJSON = course.getCourseId();
        }

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
     * @return the componentType
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * @param componentType the componentType to set
     */
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    @Override
    public String toString() {
        String jsonCourse = "{}";

        ObjectWriter ow = ooapiObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            jsonCourse = ow.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ComponentDTO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jsonCourse;
    }

    /**
     * @return the courseJSON
     */
    public String getCourseJSON() {
        if (course != null) {
            return course.getCourseId();
        }
        return "";
    }

}
