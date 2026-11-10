package oeapi.payload;

import java.time.OffsetDateTime;
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
import oeapi.model.Person;
import oeapi.model.Programme;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiLanguageTypedString;
import oeapi.validation.ValidEnumYaml;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"courseId", "primaryCode", "name", "abbreviation", "description", "teachingLanguages", "level", "fieldsOfStudy"})

public class CourseDTO extends oeapiEducationDTO {
    static Logger logger = LoggerFactory.getLogger(CourseDTO.class);

    @JsonProperty(value = "courseId")
    private String courseId = UUID.randomUUID().toString();

    @JsonProperty("programmeIds")
    private List<String> programmeIds;

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Programme> programmes;

    @JsonProperty("level")
    @ValidEnumYaml(yamlfile = "levelType.yml")
    private String level;

    @JsonProperty("fieldsOfStudy")
    private String fieldsOfStudyId;
    
    @JsonProperty("duration")
    private String duration;
    

    private String link;

    @JsonProperty("firstStartDate")
    private OffsetDateTime firstStartDate;

    @JsonProperty("admissionRequirements")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> admissionRequirements;

    @JsonProperty("qualificationRequirements")
    @Convert(converter = oeapiUnitaLanguageTypedStringConverter.class)
    private List<oeapiLanguageTypedString> qualificationRequirements;


    // OEAPI v6: studyLoad is an array of StudyLoadDescriptor. This endpoint keeps one
    // study load per resource; the JSON exposes it as a list of zero or one element.
    @JsonIgnore
    private StudyLoad studyLoad;

    @JsonIgnore
    public StudyLoad getStudyLoad() {
        return studyLoad;
    }

    @JsonIgnore
    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoad = studyLoad;
    }

    @JsonProperty("studyLoad")
    public java.util.List<StudyLoad> studyLoadAsList() {
        return studyLoad == null ? null : java.util.List.of(studyLoad);
    }

    @JsonProperty("studyLoad")
    public void studyLoadFromList(java.util.List<StudyLoad> studyLoads) {
        this.studyLoad = (studyLoads == null || studyLoads.isEmpty()) ? null : studyLoads.get(0);
    }

    @JsonProperty("coordinatorIds")
    private List<String> coordinatorIds;

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Person> coordinators;

    /**
     * @return the programmes
     */
    public List<Programme> getProgrammes() {
        return programmes;
    }

    public void setProgrammes(List<Programme> programmes) {
        this.programmes = programmes;
        List<String> programmeIds = new ArrayList<String>();
        for (Programme programme : programmes) {
            programmeIds.add(programme.getProgrammeId());
        }
        this.programmeIds = programmeIds;

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

    public List<String> getProgrammeIds() {
        return programmeIds;
    }

    public void setProgrammeIds(List<String> programmeIds) {
        this.programmeIds = programmeIds;
        List<Programme> programmes = new ArrayList<Programme>();
        for (String id : programmeIds) {
            programmes.add(new Programme(id));
        }
        this.programmes = programmes;
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
    public OffsetDateTime getFirstStartDate() {
        return firstStartDate;
    }

    /**
     * @param firstStartDate the firstStartDate to set
     */
    public void setFirstStartDate(OffsetDateTime firstStartDate) {
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
