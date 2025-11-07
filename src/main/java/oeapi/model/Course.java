package oeapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import oeapi.validation.ValidLanguageTypedString;
import oeapi.validation.ValidObjectYaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "course")
//@JsonPropertyOrder({"courseId", "primaryCode", "name", "abbreviation", "description", "teachingLanguage", "level"})
public class Course extends oeapiEducation {

    private static Logger logger = LoggerFactory.getLogger(Course.class);

    @Id
    @Column(name = "course_id", updatable = false, nullable = false)
    //@JsonProperty("courseId")
    private String courseId = UUID.randomUUID().toString();

    //@ValidEnumYaml(yamlfile = "levelType.yml")
    //private String level;
    private Long levelId;

    private String link;

    @JsonProperty("fieldsOfStudy")
    private String fieldsOfStudyId;

    private LocalDate firstStartDate;

    @JsonProperty("admissionRequirements")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true, message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> admissionRequirements;

    @JsonProperty("qualificationRequirements")
    @Column(columnDefinition = "text")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    @ValidLanguageTypedString(isNull = true, message = "Null or Invalid language-typed string elements")
    private List<oeapiLanguageTypedString> qualificationRequirements;

    @JsonProperty("studyLoad")
    @ValidObjectYaml(yamlfile = "studyLoad.yaml")
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "studyload_id")
    private StudyLoad studyLoad;

    @JsonProperty("coordinators")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable
    private List<Person> coordinators;

    @JsonProperty("programs")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable
    private List<Program> programs;

    @JsonProperty("educationSpecification")
    @ManyToOne
    @JoinColumn(name = "education_specification_id", nullable = true)
    //@JsonBackReference
    private EducationSpecification educationSpecification;

    @JsonProperty("organization")
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = true)
    //@JsonBackReference
    @JsonBackReference("courseOrganization")
    private Organization organization;

    // To do
    // Manage TimelineOverrideCourse attribute
    public Course() {
    }

    // Mantain Id if supplied
    @JsonCreator
    public Course(String id) {
        this.courseId = id;
    }

    /**
     * @return the courseId
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * @param courseId the courseId to set
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * @return the fieldsOfStudyId
     */
    public String getFieldsOfStudyId() {
        return fieldsOfStudyId;
    }

    /**
     * @param fieldsOfStudyId the fieldsOfStudyId to set
     */
    public void setFieldsOfStudyId(String fieldsOfStudyId) {
        this.fieldsOfStudyId = fieldsOfStudyId;
    }

    /**
     * @return the levelId
     */
    public Long getLevelId() {
        return levelId;
    }

    /**
     * @param levelId the levelId to set
     */
    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    /**
     * @return the programs
     */
    public List<Program> getPrograms() {
        return programs;
    }

    /**
     * @param programs the programs to set
     */
    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }

    /**
     * @return the educationSpecification
     */
    public EducationSpecification getEducationSpecification() {
        return educationSpecification;
    }

    /**
     * @param educationSpecification the educationSpecification to set
     */
    public void setEducationSpecification(EducationSpecification educationSpecification) {
        this.educationSpecification = educationSpecification;
    }

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
     * @return the level
     */
    /*
    public String getLevel() {
        return level;
    }

     */
    /**
     * @param level the level to set
     */
    /*
    public void setLevel(String level) {
        this.level = level;
    }
     */
    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the firstStartDate
     */
    public LocalDate getFirstStartDate() {
        return firstStartDate;
    }

    /**
     * @param firstStartDate the firstStartDate to set
     */
    public void setFirstStartDate(LocalDate firstStartDate) {
        this.firstStartDate = firstStartDate;
    }

    /**
     * @return the admissionRequirements
     */
    public List<oeapiLanguageTypedString> getAdmissionRequirements() {
        return admissionRequirements;
    }

    /**
     * @param admissionRequirements the admissionRequirements to set
     */
    public void setAdmissionRequirements(List<oeapiLanguageTypedString> admissionRequirements) {
        this.admissionRequirements = admissionRequirements;
    }

    /**
     * @return the qualificationRequirements
     */
    public List<oeapiLanguageTypedString> getQualificationRequirements() {
        return qualificationRequirements;
    }

    /**
     * @param qualificationRequirements the qualificationRequirements to set
     */
    public void setQualificationRequirements(List<oeapiLanguageTypedString> qualificationRequirements) {
        this.qualificationRequirements = qualificationRequirements;
    }

    /**
     * @return the studyLoad
     */
    public StudyLoad getStudyLoad() {
        return studyLoad;
    }

    /**
     * @param studyLoad the studyLoad to set
     */
    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoad = studyLoad;
    }

    /**
     * @return the coordinators
     */
    public List<Person> getCoordinators() {
        return coordinators;
    }

    /**
     * @param coordinators the coordinators to set
     */
    public void setCoordinators(List<Person> coordinators) {
        this.coordinators = coordinators;
    }

    @Override
    public String toString() {
        String jsonCourse = "{}";

        ObjectWriter ow = ooapiObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            jsonCourse = ow.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            logger.error("Course toString Error: " + ex);
        }

        return jsonCourse;
    }

}
