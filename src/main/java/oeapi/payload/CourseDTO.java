package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Convert;
import oeapi.converter.oeapiUnitaLanguageTypedStringConverter;

import oeapi.model.Consumer;
import oeapi.model.EducationSpecification;
import oeapi.model.Organization;
import oeapi.model.Person;
import oeapi.model.Program;
import oeapi.model.StudyLoad;
import oeapi.model.oeapiLanguageTypedString;
import static oeapi.oeapiUtils.ooapiObjectMapper;
import oeapi.validation.ValidEnumYaml;
import oeapi.validation.ValidFieldsOfStudy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"courseId", "primaryCode", "name", "abbreviation", "description", "teachingLanguage", "level", "fieldsOfStudy"})

public class CourseDTO extends oeapiEducationDTO {

    static Logger logger = LoggerFactory.getLogger(CourseDTO.class);

    @JsonProperty(value = "courseId")
    private String courseId = UUID.randomUUID().toString();

    @JsonProperty("programs")
    private List<String> programsJSON;

    @JsonIgnore
    private List<Program> programs;

    @JsonProperty("educationSpecification")
    private String educationSpecificationJSON;

    @JsonIgnore
    private EducationSpecification educationSpecification;

    // Aproach to expandable
    //@JsonProperty("organization")
    //private String organizationId;
    //@JsonProperty("organizationObject")
    //private Organization organization;
    @JsonProperty("organization")
    private String organizationJSON;

    @JsonIgnore
    private Organization organization;

    @JsonProperty("consumers")
    private List<Consumer> consumers;

    @JsonProperty("learningOutcomes")
    private List<List<oeapiLanguageTypedString>> learningOutcomes;

    @JsonProperty("level")
    @ValidEnumYaml(yamlfile = "levelType.yml")
    private String level;

    @JsonProperty("fieldsOfStudy")
    private String fieldsOfStudyId;

    @ValidEnumYaml(yamlfile = "sectorType.yml")
    private String sector;
    private String link;
    @JsonIgnore
    private String firstStartDateJSON;
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
    private List<String> coordinatorsJSON;

    //@JsonProperty("coordinatorsObject")
    @JsonIgnore
    private List<Person> coordinators;

    /**
     * @return the educationSpecificationJSON
     */
    public String getEducationSpecificationJSON() {
        return educationSpecificationJSON;
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
       if (organization != null)
       {    
        this.organization = organization;            
        this.organizationJSON = organization.getOrganizationId();
       }
    }
    
     /**
     * @param organization the organization to set
     */
    public void setOrganizationJSON(String organizationId) {
       if (organizationId != null)
       {    
        this.organizationJSON = organizationId;    
        this.organization = new Organization(organizationId);
       } 
    }   
    
    /**
     * @return the organizationJSON
     */
    public String getOrganizationJSON() {
        return organizationJSON;
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
        List<String> programsJSON = new ArrayList<String>();
        for (Program currProgram : programs) 
        {
            String progID = currProgram.getProgramId();
            programsJSON.add(progID);
        }
        this.programsJSON = programsJSON;

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

    /**
     * @return the programsJSON
     */
    public List<String> getProgramsJSON() {
        return programsJSON;
    }

    /**
     * @param programsJSON the programsJSON to set
     */
    public void setProgramsJSON(List<String> programsJSON) {
        this.programsJSON = programsJSON;
        List<Program> programs = new ArrayList<Program>();
        for (String currProgram : programsJSON) 
         { 
            Program aProgram = new Program(currProgram);
            programs.add(aProgram);
         } 
        this.programs = programs;
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
     * @return the learningOutcomes
     */
    public List<List<oeapiLanguageTypedString>> getLearningOutcomes() {
        return learningOutcomes;
    }

    /**
     * @param learningOutcomes the learningOutcomes to set
     */
    public void setLearningOutcomes(List<List<oeapiLanguageTypedString>> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
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
     * @return the firstStartDateJSON
     */
    public String getFirstStartDateJSON() {
        return firstStartDateJSON;
    }

    /**
     * @param firstStartDateJSON the firstStartDateJSON to set
     */
    public void setFirstStartDateJSON(String firstStartDateJSON) {
        this.firstStartDateJSON = firstStartDateJSON;
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
        List<String> coordinatorsJSON = new ArrayList<>();
        for (Person p : coordinators) {
            coordinatorsJSON.add(p.getPersonId());
        }
        this.coordinatorsJSON = coordinatorsJSON;

//        this.setCoordinatorsJSON(coordinators.stream()
//                .map(Person::getPersonId)
//                .collect(Collectors.toList()));

    }

    /**
     * @param coordinatorsJSON the programsJSON to set
     */
    public void setCoordinatorsJSON(List<String> coordinatorsJSON) {
        this.coordinatorsJSON = coordinatorsJSON;
        List<Person> coordinators = new ArrayList<Person>();
        for (String currPersonId : coordinatorsJSON) 
         { 
            Person aPerson = new Person(currPersonId);
            coordinators.add(aPerson);
         } 
        this.coordinators = coordinators;        
    }

    public List<String> getCoordinatorsJSON() {

        return coordinatorsJSON;

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
