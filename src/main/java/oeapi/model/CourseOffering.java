package oeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author itziar.urrutia
 */
@Entity
@DiscriminatorValue("CourseOffering")
@JsonPropertyOrder({"offeringId", "primaryCode", "offeringType", "name", "description", "teachingLanguage", "resultExpected"})
public class CourseOffering extends Offering {

    @JsonProperty("course")
    @ManyToOne
    @JoinColumn(name = "course_id", unique = false, nullable = true)
    private Course course;

    @JsonProperty("programmeOffering")
    @ManyToOne
    @JoinColumn(name = "programme_offering_id", unique = false, nullable = true)
    private ProgrammeOffering programmeOffering;

    
    @Override
    public String getOfferingType() {
        return "course";  
    }    
    
    
    /**
     * @return the course
     */
    public Course getCourse() {
        return course;
    }

    /**
     * @param course the course to set
     */
    public void setCourse(Course course) {
        this.course = course;
    }

    /**
     * @return the programmeOffering
     */
    public ProgrammeOffering getProgrammeOffering() {
        return programmeOffering;
    }

    /**
     * @param programmeOffering the programmeOffering to set
     */
    public void setProgrammeOffering(ProgrammeOffering programmeOffering) {
        this.programmeOffering = programmeOffering;
    }

}
