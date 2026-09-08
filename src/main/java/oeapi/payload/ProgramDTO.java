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

    // coordinators

    @JsonProperty("coordinators")
    private List<String> coordinatorIds;

    public List<String> getCoordinatorIds() {
        return coordinatorIds;
    }

    public void setCoordinatorIds(List<String> coordinatorIds) {
        this.coordinatorIds = coordinatorIds;
        this.coordinators = oeapiDTORefs.keepOrStub(this.coordinators, coordinatorIds, Person::getPersonId, Person::new);
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

        // Never discard an already mapped parent for a stub built from its own id: see
        // oeapiDTORefs. Without this ?expand=parent answers with a bare programId whenever
        // ModelMapper happens to call this setter after setParent().
        this.parent = oeapiDTORefs.keepOrStub(this.parent, parentId, Program::getProgramId, Program::new);
    }

    @JsonIgnore
    @oeapiDTOExpandable
    public Program parent;

    public Program getParent() {
        return parent;
    }

    public void setParent(Program parent) {
        this.parent = parent;
        this.parentId = parent == null ? null : parent.getProgramId();
    }

    @JsonProperty("children")
    private List<String> childrenIds;

    public List<String> getChildrenIds() {
        return childrenIds;
    }

    public void setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds;
        this.children = oeapiDTORefs.keepOrStub(this.children, childrenIds, Program::getProgramId, Program::new);
    }

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Program> children;

    public List<Program> getChildren() {
        return children;
    }

    public void setChildren(List<Program> children) {
        this.children = children;

        // The id counterpart was missing here, so a read that went through this setter left
        // childrenIds null and the unexpanded program carried no "children" at all.
        this.childrenIds = oeapiDTORefs.idsOf(children, Program::getProgramId);
    }
}
