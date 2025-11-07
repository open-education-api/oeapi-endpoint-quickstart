/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import oeapi.model.Course;
import oeapi.model.Organization;

/**
 *
 * @author itziar.urrutia
 */
public class CourseOfferingDTO extends OfferingDTO {

    /**
     * @return the courseJSON
     */
    public String getCourseJSON() {
        return (course == null) ? null : course.getCourseId();
    }

    /**
     * @param courseJSON the courseJSON to set
     */
    public void setCourseJSON(String courseJSON) {
        this.courseJSON = courseJSON;
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

    @JsonProperty("course")
    private String courseJSON;

    @JsonIgnore
    private Course course;

    @Override
    public String getOfferingType() {
        return "course";
    }

    @Override
    public String getOrganizationJSON() {

        if (super.getOrganizationJSON() == null) {
            return (course == null || course.getOrganization() == null) ? null : course.getOrganization().getOrganizationId();
        }
        return super.getOrganizationJSON();

    }

    @Override
    public String getTeachingLanguage() {

        if (super.getTeachingLanguage() == null) {
            return (course == null) ? null : course.getTeachingLanguage();
        }
        return super.getTeachingLanguage();

    }

}
