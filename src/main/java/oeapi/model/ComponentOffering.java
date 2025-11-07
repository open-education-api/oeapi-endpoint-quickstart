package oeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author itziar.urrutia
 */
@Entity
@DiscriminatorValue("ComponentOffering")
public class ComponentOffering extends Offering {

    @JsonProperty("component")
    @ManyToOne
    @JoinColumn(name = "component_id", unique = false, nullable = true)
    private Component component;

    @JsonProperty("room")
    @ManyToOne
    @JoinColumn(name = "room_id", unique = false, nullable = true)
    private Room room;

    private int resultWeight;

    @JsonProperty("courseOffering")
    @ManyToOne
    @JoinColumn(name = "course_offering_id", unique = false, nullable = true)
    private CourseOffering courseOffering;

    
    @Override
    public String getOfferingType() {
        return "component";  // Explicitly return the type
    }    
    
    
    /**
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * @param component the component to set
     */
    public void setComponent(Component component) {
        this.component = component;
    }

    /**
     * @return the room
     */
    public Room getRoom() {
        return room;
    }

    /**
     * @param room the room to set
     */
    public void setRoom(Room room) {
        this.room = room;
    }

    /**
     * @return the resultWeight
     */
    public int getResultWeight() {
        return resultWeight;
    }

    /**
     * @param resultWeight the resultWeight to set
     */
    public void setResultWeight(int resultWeight) {
        this.resultWeight = resultWeight;
    }

    /**
     * @return the courseOffering
     */
    public CourseOffering getCourseOffering() {
        return courseOffering;
    }

    /**
     * @param courseOffering the courseOffering to set
     */
    public void setCourseOffering(CourseOffering courseOffering) {
        this.courseOffering = courseOffering;
    }

}
