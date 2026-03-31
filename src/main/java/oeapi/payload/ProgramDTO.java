package oeapi.payload;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Convert;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.model.Course;
import oeapi.model.EducationSpecification;
import oeapi.model.Organization;
import oeapi.model.Person;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiLanguageTypedString;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"programId", "primaryCode", "name", "abbreviation", "description", "programType", "teachingLanguage", "level"})

public class ProgramDTO extends oeapiEducationDTO {
    static Logger logger = LoggerFactory.getLogger(ProgramDTO.class);

    @JsonProperty("programId")
    private String programId = UUID.randomUUID().toString();

    @JsonProperty("programType")
    private String programType;

    @JsonProperty("courses")
    private List<String> courseIds;

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Course> courses;

    @JsonProperty("admissionRequirements")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> admissionRequirements;

    @JsonProperty("qualificationRequirements")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> qualificationRequirements;

    @JsonProperty("studyLoad")
    private StudyLoad studyLoad;

    @JsonProperty("coordinators")
    private List<String> coordinatorIds;

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Person> coordinators;

    @JsonIgnore
    @oeapiDTOExpandable
    private EducationSpecification educationSpecification;

    @JsonProperty("educationSpecification")
    private EducationSpecification educationSpecificationId;

    @JsonProperty("organization")
    private String organizationId;

    @JsonIgnore
    @oeapiDTOExpandable
    public Organization organization;

    @JsonProperty("level")
    private String level;

    @JsonProperty("sector")
    private String sector;

    @JsonProperty("firstStartDate")
    private LocalDate firstStartDate;

    @JsonProperty("fieldsOfStudy")
    private String fieldsOfStudyId;

    public String getFieldsOfStudyId() {
        return fieldsOfStudyId;
    }

    public void setFieldsOfStudyId(String fieldsOfStudyId) {
        this.fieldsOfStudyId = fieldsOfStudyId;
    }

    /**
     * @return the programId
     */
    public String getProgramId() {
        return programId;
    }

    /**
     * @param programId the programId to set
     */
    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public void setProgramId() {
        this.setProgramId(UUID.randomUUID().toString());
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        if (courses == null) courses = Collections.emptyList();

        this.courses = courses;
        List<String> courseIds = new ArrayList<String>();
        for (Course course : this.courses) {
            courseIds.add(course.getCourseId());
        }
        this.courseIds = courseIds;
    }

    public List<String> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
        List<Course> courses = new ArrayList<Course>();
        for (String id : courseIds) {
            courses.add(new Course(id));
        }
        this.courses = courses;
    }
}
