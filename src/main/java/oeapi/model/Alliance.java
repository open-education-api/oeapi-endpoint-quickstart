package oeapi.model;

import java.time.LocalTime;
import java.util.List;
import javax.persistence.Convert;
import oeapi.converter.oeapiListStringConverter;

/**
 *
 * @author itziar.urrutia
 */
public class Alliance {

    private String name;

    private String enrollmentForGuests;

    private boolean visibleForGuests;

    private String enrollmentForOwnStudents;

    private String type;

    private boolean visibleForOwnStudents;

    private boolean selection;

    @Convert(converter = oeapiListStringConverter.class)
    private List<String> jointPartnerCodes; // For program

    @Convert(converter = oeapiListStringConverter.class)
    private List<String> instructorNames; // For program

    private Float contactHours;
    private String activities;

    @Convert(converter = oeapiListStringConverter.class)
    private List<String> themes;

    private String enrollmentUrl;
    private LocalTime enrollStartTime;
    private LocalTime enrollDateTime;
    private String dateComment;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the enrollmentForGuests
     */
    public String getEnrollmentForGuests() {
        return enrollmentForGuests;
    }

    /**
     * @param enrollmentForGuests the enrollmentForGuests to set
     */
    public void setEnrollmentForGuests(String enrollmentForGuests) {
        this.enrollmentForGuests = enrollmentForGuests;
    }

    /**
     * @return the visibleForGuests
     */
    public boolean isVisibleForGuests() {
        return visibleForGuests;
    }

    /**
     * @param visibleForGuests the visibleForGuests to set
     */
    public void setVisibleForGuests(boolean visibleForGuests) {
        this.visibleForGuests = visibleForGuests;
    }

    /**
     * @return the enrollmentForOwnStudents
     */
    public String getEnrollmentForOwnStudents() {
        return enrollmentForOwnStudents;
    }

    /**
     * @param enrollmentForOwnStudents the enrollmentForOwnStudents to set
     */
    public void setEnrollmentForOwnStudents(String enrollmentForOwnStudents) {
        this.enrollmentForOwnStudents = enrollmentForOwnStudents;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the visibleForOwnStudents
     */
    public boolean isVisibleForOwnStudents() {
        return visibleForOwnStudents;
    }

    /**
     * @param visibleForOwnStudents the visibleForOwnStudents to set
     */
    public void setVisibleForOwnStudents(boolean visibleForOwnStudents) {
        this.visibleForOwnStudents = visibleForOwnStudents;
    }

    /**
     * @return the selection
     */
    public boolean isSelection() {
        return selection;
    }

    /**
     * @param selection the selection to set
     */
    public void setSelection(boolean selection) {
        this.selection = selection;
    }

    /**
     * @return the jointPartnerCodes
     */
    public List<String> getJointPartnerCodes() {
        return jointPartnerCodes;
    }

    /**
     * @param jointPartnerCodes the jointPartnerCodes to set
     */
    public void setJointPartnerCodes(List<String> jointPartnerCodes) {
        this.jointPartnerCodes = jointPartnerCodes;
    }

    /**
     * @return the themes
     */
    public List<String> getThemes() {
        return themes;
    }

    /**
     * @param themes the themes to set
     */
    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

    /**
     * @return the instructorNames
     */
    public List<String> getInstructorNames() {
        return instructorNames;
    }

    /**
     * @param instructorNames the instructorNames to set
     */
    public void setInstructorNames(List<String> instructorNames) {
        this.instructorNames = instructorNames;
    }

    /**
     * @return the contactHours
     */
    public Float getContactHours() {
        return contactHours;
    }

    /**
     * @param contactHours the contactHours to set
     */
    public void setContactHours(Float contactHours) {
        this.contactHours = contactHours;
    }

    /**
     * @return the activities
     */
    public String getActivities() {
        return activities;
    }

    /**
     * @param activities the activities to set
     */
    public void setActivities(String activities) {
        this.activities = activities;
    }

    /**
     * @return the enrollmentUrl
     */
    public String getEnrollmentUrl() {
        return enrollmentUrl;
    }

    /**
     * @param enrollmentUrl the enrollmentUrl to set
     */
    public void setEnrollmentUrl(String enrollmentUrl) {
        this.enrollmentUrl = enrollmentUrl;
    }

    /**
     * @return the enrollStartTime
     */
    public LocalTime getEnrollStartTime() {
        return enrollStartTime;
    }

    /**
     * @param enrollStartTime the enrollStartTime to set
     */
    public void setEnrollStartTime(LocalTime enrollStartTime) {
        this.enrollStartTime = enrollStartTime;
    }

    /**
     * @return the enrollDateTime
     */
    public LocalTime getEnrollDateTime() {
        return enrollDateTime;
    }

    /**
     * @param enrollDateTime the enrollDateTime to set
     */
    public void setEnrollDateTime(LocalTime enrollDateTime) {
        this.enrollDateTime = enrollDateTime;
    }

    /**
     * @return the dateComment
     */
    public String getDateComment() {
        return dateComment;
    }

    /**
     * @param dateComment the dateComment to set
     */
    public void setDateComment(String dateComment) {
        this.dateComment = dateComment;
    }

}
