package oeapi.payload;

import static oeapi.oeapiUtils.ooapiObjectMapper;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectWriter;

import oeapi.model.Course;
import oeapi.validation.ValidEnumYaml;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"componentId", "primaryCode", "name", "abbreviation", "componentType", "teachingLanguages"})
public class ComponentDTO extends oeapiEducationDTO {

    @JsonProperty("componentId")
    private String componentId = UUID.randomUUID().toString();

    @JsonProperty("componentType")
    @ValidEnumYaml(yamlfile = "learningComponentType.yml")
    private String componentType;

    // OEAPI v6: "courseId" holds the id, "course" the object when ?expand=course
    @JsonProperty("courseId")
    private String courseId;

    @JsonIgnore
    @oeapiDTOExpandable
    public Course course;

    @JsonIgnore
    public Course getCourse() {
        return course;
    }

    @JsonIgnore
    public void setCourse(Course course) {
        this.course = course;
        this.courseId = course == null ? null : course.getCourseId();
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
        if (courseId == null) {
            this.course = null;
        } else if (this.course == null || !courseId.equals(this.course.getCourseId())) {
            this.course = new Course(courseId);
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
        } catch (JacksonException ex) {
            Logger.getLogger(ComponentDTO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jsonCourse;
    }

}
