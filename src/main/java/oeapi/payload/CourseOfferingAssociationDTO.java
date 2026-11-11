package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import oeapi.model.StudyLoad;

/**
 * OEAPI v6 CourseOfferingAssociation.
 */
public class CourseOfferingAssociationDTO extends OfferingAssociationDTO {

    @JsonProperty("courseOfferingId")
    public String courseOfferingId() {
        return getOfferingId();
    }

    @JsonProperty("courseOfferingId")
    public void courseOfferingId(String id) {
        setOfferingId(id);
    }

    // Course and programme offering associations carry a study load
    @JsonProperty("studyLoad")
    private StudyLoad studyLoad;

    public StudyLoad getStudyLoad() {
        return studyLoad;
    }

    public void setStudyLoad(StudyLoad studyLoad) {
        this.studyLoad = studyLoad;
    }
}
