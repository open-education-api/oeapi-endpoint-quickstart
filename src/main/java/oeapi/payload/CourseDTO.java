package oeapi.payload;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Convert;

import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;
import oeapi.model.EducationSpecification;
import oeapi.model.Person;
import oeapi.model.Program;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiLanguageTypedString;
import oeapi.validation.ValidEnumYaml;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"courseId", "primaryCode", "name", "abbreviation", "description", "teachingLanguage", "level", "fieldsOfStudy"})

public class CourseDTO extends oeapiEducationDTO {
    static Logger logger = LoggerFactory.getLogger(CourseDTO.class);

    @JsonProperty(value = "courseId")
    private String courseId = UUID.randomUUID().toString();

    @JsonProperty("programs")
    private List<String> programIds;

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Program> programs;

    @JsonProperty("educationSpecification")
    private String educationSpecificationId;

    @JsonIgnore
    @oeapiDTOExpandable
    public EducationSpecification educationSpecification;

    @JsonProperty("level")
    @ValidEnumYaml(yamlfile = "levelType.yml")
    private String level;

    @JsonProperty("fieldsOfStudy")
    private String fieldsOfStudyId;
    
    @JsonProperty("duration")
    private String duration;
    

    @ValidEnumYaml(yamlfile = "sectorType.yml")
    private String sector;
    private String link;

    @JsonProperty("firstStartDate")
    private LocalDate firstStartDate;

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

    /**
     * @return the educationSpecificationId
     */
    public String getEducationSpecificationId() {
        return educationSpecificationId;
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
     * @return the programs
     */
    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
        List<String> programIds = new ArrayList<String>();
        for (Program program : programs) {
            programIds.add(program.getProgramId());
        }
        this.programIds = programIds;

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

    public void setCourseId() {
        this.setCourseId(UUID.randomUUID().toString());
    }

    public List<String> getProgramIds() {
        return programIds;
    }

    public void setProgramIds(List<String> programIds) {
        this.programIds = programIds;
        List<Program> programs = new ArrayList<Program>();
        for (String id : programIds) {
            programs.add(new Program(id));
        }
        this.programs = programs;
    }

    /**
     * @return the modeOfDelivery
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
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * @return the sector
     */
    public String getSector() {
        return sector;
    }

    /**
     * @param sector the sector to set
     */
    public void setSector(String sector) {
        this.sector = sector;
    }

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

    public List<Person> getCoordinators() {
        return coordinators;
    }

    public void setCoordinators(List<Person> coordinators) {
        this.coordinators = coordinators;
        List<String> coordinatorIds = new ArrayList<>();
        for (Person p : coordinators) {
            coordinatorIds.add(p.getPersonId());
        }
        this.coordinatorIds = coordinatorIds;
    }

    public void setCoordinatorIds(List<String> coordinatorIds) {
        this.coordinatorIds = coordinatorIds;
        List<Person> coordinators = new ArrayList<Person>();
        for (String currPersonId : coordinatorIds) {
            Person aPerson = new Person(currPersonId);
            coordinators.add(aPerson);
        }
        this.coordinators = coordinators;
    }

    public List<String> getCoordinatorIds() {
        return coordinatorIds;
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
}
