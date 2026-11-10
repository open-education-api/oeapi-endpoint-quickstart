package oeapi.payload;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import oeapi.model.Component;
import oeapi.model.CourseOffering;
import oeapi.model.Room;

/**
 * OEAPI v6 LearningComponentOffering (entity: ComponentOffering).
 */
@JsonPropertyOrder({"learningComponentOfferingId", "primaryCode", "learningComponentId", "courseOfferingIds", "roomIds"})
public class LearningComponentOfferingDTO extends OfferingDTO {

    @JsonProperty("learningComponentId")
    private String learningComponentId;

    // Named after the v6 expand value (learning_component); getComponent() and
    // setComponent() keep the entity property name for ModelMapper.
    @JsonIgnore
    @oeapiDTOExpandable
    public Component learningComponent;

    // The entity keeps one room and one course offering; v6 publishes lists of ids
    @JsonIgnore
    private Room room;

    @JsonIgnore
    private CourseOffering courseOffering;

    @JsonProperty("resultWeight")
    private Integer resultWeight;

    @JsonProperty("learningComponentOfferingId")
    // Not named getLearningComponentOfferingId(): ModelMapper must not see a second candidate for offeringId
    public String learningComponentOfferingId() {
        return getOfferingId();
    }

    @JsonProperty("learningComponentOfferingId")
    public void learningComponentOfferingId(String id) {
        setOfferingId(id);
    }

    @JsonIgnore
    public Component getComponent() {
        return learningComponent;
    }

    @JsonIgnore
    public void setComponent(Component component) {
        this.learningComponent = component;
        this.learningComponentId = component == null ? null : component.getComponentId();
    }

    @JsonProperty("learningComponentId")
    public String learningComponentId() {
        return learningComponentId;
    }

    @JsonProperty("learningComponentId")
    public void learningComponentId(String learningComponentId) {
        this.learningComponentId = learningComponentId;
        if (learningComponentId == null) {
            this.learningComponent = null;
        } else if (this.learningComponent == null || !learningComponentId.equals(this.learningComponent.getComponentId())) {
            Component stub = new Component();
            stub.setComponentId(learningComponentId);
            this.learningComponent = stub;
        }
    }

    @JsonIgnore
    public Room getRoom() {
        return room;
    }

    @JsonIgnore
    public void setRoom(Room room) {
        this.room = room;
    }

    @JsonIgnore
    public CourseOffering getCourseOffering() {
        return courseOffering;
    }

    @JsonIgnore
    public void setCourseOffering(CourseOffering courseOffering) {
        this.courseOffering = courseOffering;
    }

    @JsonProperty("roomIds")
    public List<String> roomIdList() {
        List<String> ids = new ArrayList<>();
        if (room != null) {
            ids.add(room.getRoomId());
        }
        return ids;
    }

    @JsonProperty("roomIds")
    public void roomIdsFromList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            this.room = null;
        } else {
            Room stub = new Room();
            stub.setRoomId(ids.get(0));
            this.room = stub;
        }
    }

    @JsonProperty("courseOfferingIds")
    public List<String> courseOfferingIdList() {
        List<String> ids = new ArrayList<>();
        if (courseOffering != null) {
            ids.add(courseOffering.getOfferingId());
        }
        return ids;
    }

    @JsonProperty("courseOfferingIds")
    public void courseOfferingIdsFromList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            this.courseOffering = null;
        } else {
            CourseOffering stub = new CourseOffering();
            stub.setOfferingId(ids.get(0));
            this.courseOffering = stub;
        }
    }

    public Integer getResultWeight() {
        return resultWeight;
    }

    public void setResultWeight(Integer resultWeight) {
        this.resultWeight = resultWeight;
    }
}
