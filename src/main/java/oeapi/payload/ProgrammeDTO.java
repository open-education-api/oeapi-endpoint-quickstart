package oeapi.payload;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import oeapi.model.Person;
import oeapi.model.Programme;
import oeapi.model.StudyLoad;
import oeapi.validation.ValidEnumYaml;
import oeapi.model.oeapiLanguageTypedString;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"programmeId", "primaryCode", "name", "abbreviation", "description", "programmeType", "teachingLanguages", "level", "parentId", "childIds"})

public class ProgrammeDTO extends oeapiEducationDTO {

    // programme id

    @JsonProperty("programmeId")
    private String programmeId = UUID.randomUUID().toString();

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    // programme type

    @JsonProperty("programmeType")
    private String programmeType;

    public String getProgrammeType() {
        return programmeType;
    }

    public void setProgrammeType(String programmeType) {
        this.programmeType = programmeType;
    }

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

    // level of qualification (EQF / NLQF)

    @JsonProperty("levelOfQualification")
    @ValidEnumYaml(yamlfile = "levelOfQualificationType.yml")
    private String levelOfQualification;

    public String getLevelOfQualification() {
        return levelOfQualification;
    }

    public void setLevelOfQualification(String levelOfQualification) {
        this.levelOfQualification = levelOfQualification;
    }

    // qualification awarded

    @JsonProperty("qualificationAwarded")
    @ValidEnumYaml(yamlfile = "qualificationAwardedType.yml")
    private String qualificationAwarded;

    public String getQualificationAwarded() {
        return qualificationAwarded;
    }

    public void setQualificationAwarded(String qualificationAwarded) {
        this.qualificationAwarded = qualificationAwarded;
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

    @JsonProperty("firstStartDateTime")
    private OffsetDateTime firstStartDate;

    public OffsetDateTime getFirstStartDate() {
        return firstStartDate;
    }

    public void setFirstStartDate(OffsetDateTime firstStartDate) {
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

    // coordinators

    @JsonProperty("coordinatorIds")
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

    @JsonProperty("parentId")
    private String parentId;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
        this.parent = parentId == null ? null : new Programme(parentId);
    }

    @JsonIgnore
    @oeapiDTOExpandable
    public Programme parent;

    public Programme getParent() {
        return parent;
    }

    public void setParent(Programme parent) {
        this.parent = parent;
        this.parentId = parent == null ? null : parent.getProgrammeId();
    }

    @JsonProperty("childIds")
    private List<String> childrenIds;

    public List<String> getChildrenIds() {
        return childrenIds;
    }

    public void setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds;
        List<Programme> children = new ArrayList<Programme>();
        for (String id : childrenIds) {
            children.add(new Programme(id));
        }
        this.children = children;
    }

    @JsonIgnore
    @oeapiDTOExpandable
    public List<Programme> children;

    public List<Programme> getChildren() {
        return children;
    }

    public void setChildren(List<Programme> children) {
        this.children = children;
    }
}
