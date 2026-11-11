package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import oeapi.model.StudyLoad;

/**
 * OEAPI v6 ProgrammeOfferingAssociation.
 */
public class ProgrammeOfferingAssociationDTO extends OfferingAssociationDTO {

    @JsonProperty("programmeOfferingId")
    public String programmeOfferingId() {
        return getOfferingId();
    }

    @JsonProperty("programmeOfferingId")
    public void programmeOfferingId(String id) {
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
