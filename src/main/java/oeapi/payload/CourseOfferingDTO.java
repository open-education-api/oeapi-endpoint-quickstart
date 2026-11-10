package oeapi.payload;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import oeapi.model.Course;
import oeapi.model.ProgrammeOffering;

/**
 * OEAPI v6 CourseOffering.
 *
 * @author itziar.urrutia
 */
@JsonPropertyOrder({"courseOfferingId", "primaryCode", "courseId", "programmeOfferingIds"})
public class CourseOfferingDTO extends OfferingDTO {

    @JsonProperty("courseId")
    private String courseId;

    @JsonIgnore
    @oeapiDTOExpandable
    public Course course;

    // The entity links a course offering to one programme offering; v6 allows several
    @JsonIgnore
    private ProgrammeOffering programmeOffering;

    @JsonProperty("flexibleEntryPeriodStartDateTime")
    private OffsetDateTime flexibleEntryPeriodStart;

    @JsonProperty("flexibleEntryPeriodEndDateTime")
    private OffsetDateTime flexibleEntryPeriodEnd;

    @JsonProperty("courseOfferingId")
    // Not named getCourseOfferingId(): ModelMapper must not see a second candidate for offeringId
    public String courseOfferingId() {
        return getOfferingId();
    }

    @JsonProperty("courseOfferingId")
    public void courseOfferingId(String id) {
        setOfferingId(id);
    }

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

    @JsonIgnore
    public ProgrammeOffering getProgrammeOffering() {
        return programmeOffering;
    }

    @JsonIgnore
    public void setProgrammeOffering(ProgrammeOffering programmeOffering) {
        this.programmeOffering = programmeOffering;
    }

    @JsonProperty("programmeOfferingIds")
    public List<String> programmeOfferingIdList() {
        List<String> ids = new ArrayList<>();
        if (programmeOffering != null) {
            ids.add(programmeOffering.getOfferingId());
        }
        return ids;
    }

    @JsonProperty("programmeOfferingIds")
    public void programmeOfferingIdsFromList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            this.programmeOffering = null;
        } else {
            ProgrammeOffering stub = new ProgrammeOffering();
            stub.setOfferingId(ids.get(0));
            this.programmeOffering = stub;
        }
    }

    public OffsetDateTime getFlexibleEntryPeriodStart() {
        return flexibleEntryPeriodStart;
    }

    public void setFlexibleEntryPeriodStart(OffsetDateTime flexibleEntryPeriodStart) {
        this.flexibleEntryPeriodStart = flexibleEntryPeriodStart;
    }

    public OffsetDateTime getFlexibleEntryPeriodEnd() {
        return flexibleEntryPeriodEnd;
    }

    public void setFlexibleEntryPeriodEnd(OffsetDateTime flexibleEntryPeriodEnd) {
        this.flexibleEntryPeriodEnd = flexibleEntryPeriodEnd;
    }

    @Override
    public List<String> getTeachingLanguages() {
        if (super.getTeachingLanguages() == null || super.getTeachingLanguages().isEmpty()) {
            return (course == null) ? null : course.getTeachingLanguages();
        }
        return super.getTeachingLanguages();
    }
}
