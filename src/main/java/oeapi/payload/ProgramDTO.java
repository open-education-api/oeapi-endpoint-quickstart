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

import oeapi.model.Course;
import oeapi.model.EducationSpecification;
import oeapi.model.Person;
import oeapi.model.Program;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiLanguageTypedString;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"programId", "primaryCode", "name", "abbreviation", "description", "programType", "teachingLanguage", "level", "parent", "children"})

public class ProgramDTO extends oeapiEducationDTO {

    // program id

    @JsonProperty("programId")
    private String programId = UUID.randomUUID().toString();

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    // program type

    @JsonProperty("programType")
    private String programType;

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    @JsonProperty("studyLoad")
    private StudyLoad studyLoad;

    public StudyLoad getStudyLoad() {
        return studyLoad;
    }

    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoad = studyLoad;
    }

    // mode of study

    @JsonProperty("modeOfStudy")
    private String modeOfStudy;

    public String getModeOfStudy() {
        return modeOfStudy;
    }

    public void setModeOfStudy(String modeOfStudy) {
        this.modeOfStudy = modeOfStudy;
    }

    // first start date

    @JsonProperty("firstStartDate")
    private LocalDate firstStartDate;

    public LocalDate getFirstStartDate() {
        return firstStartDate;
    }

    public void setFirstStartDate(LocalDate firstStartDate) {
        this.firstStartDate = firstStartDate;
    }

    // level

    @JsonProperty("level")
    private String level;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    // sector

    @JsonProperty("sector")
    private String sector;

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    // fields of study

    @JsonProperty("fieldsOfStudy")
    private String fieldsOfStudyId;

    public String getFieldsOfStudyId() { return fieldsOfStudyId; }

    public void setFieldsOfStudyId(String fieldsOfStudyId) {
        this.fieldsOfStudyId = fieldsOfStudyId;
    }

    // admission requirements

    @JsonProperty("admissionRequirements")
    private List<oeapiLanguageTypedString> admissionRequirements;

    public List<oeapiLanguageTypedString> getAdmissionRequirements() {
        return admissionRequirements;
    }

    public void setAdmissionRequirements(List<oeapiLanguageTypedString> admissionRequirements) {
        this.admissionRequirements = admissionRequirements;
    }

    // qualifations requirements

    @JsonProperty("qualificationRequirements")
    private List<oeapiLanguageTypedString> qualificationRequirements;

    public List<oeapiLanguageTypedString> getQualificationRequirements() {
        return qualificationRequirements;
    }

    public void setQualificationRequirements(List<oeapiLanguageTypedString> qualificationRequirements) {
        this.qualificationRequirements = qualificationRequirements;
    }

    // link

    @JsonProperty("link")
    private String link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    // education specification

    @JsonIgnore
    @oeapiDTOExpandable
    public EducationSpecification educationSpecification;

    public EducationSpecification getEducationSpecification() {
        return educationSpecification;
    }

    public void setEducationSpecification(EducationSpecification educationSpecification) {
        this.educationSpecification = educationSpecification;
    }

    @JsonProperty("educationSpecification")
    private EducationSpecification educationSpecificationId;

    public EducationSpecification getEducationSpecificationId() {
        return educationSpecificationId;
    }

    public void setEducationSpecificationId(EducationSpecification educationSpecificationId) {
        this.educationSpecificationId = educationSpecificationId;
    }

    // courses

    @JsonProperty("courses")
    private List<String> courseIds;

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Course> courses;

    public List<Course> getCourses() { return courses; }

    public void setCourses(List<Course> courses) {
        if (courses == null) courses = Collections.emptyList();

        this.courses = courses;
        List<String> courseIds = new ArrayList<String>();
        for (Course course : this.courses) {
            courseIds.add(course.getCourseId());
        }
        this.courseIds = courseIds;
    }

    public List<String> getCourseIds() { return courseIds;    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
        List<Course> courses = new ArrayList<Course>();
        for (String id : courseIds) {
            courses.add(new Course(id));
        }
        this.courses = courses;
    }

    // coordinators

    @JsonProperty("coordinators")
    private List<String> coordinatorIds;

    public List<String> getCoordinatorIds() {
        return coordinatorIds;
    }

    public void setCoordinatorIds(List<String> coordinatorIds) {
        this.coordinatorIds = coordinatorIds;
        List<Person> coordinators = new ArrayList<Person>();
        for (String id : coordinatorIds) {
            coordinators.add(new Person(id));
        }
        this.coordinators = coordinators;
    }

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Person> coordinators;

    public List<Person> getCoordinators() {
        return coordinators;
    }

    public void setCoordinators(List<Person> coordinators) {
        if (coordinators == null) coordinators = Collections.emptyList();

        this.coordinators = coordinators;
        List<String> coordinatorIds = new ArrayList<String>();
        for (Person person : coordinators) {
            coordinatorIds.add(person.getPersonId());
        }
        this.coordinatorIds = coordinatorIds;
    }

    // parent and children

    @JsonProperty("parent")
    private String parentId;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @JsonIgnore
    @oeapiDTOExpandable
    public Program parent;

    public Program getParent() {
        return parent;
    }

    public void setParent(Program parent) {
        this.parent = parent;
    }

    @JsonProperty("children")
    private List<String> childrenIds;

    public List<String> getChildrenIds() {
        return childrenIds;
    }

    public void setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds;
        List<Program> children = new ArrayList<Program>();
        for (String id : childrenIds) {
            children.add(new Program(id));
        }
        this.children = children;
    }

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Program> children;

    public List<Program> getChildren() {
        return children;
    }

    public void setChildren(List<Program> children) {
        this.children = children;
    }
}
