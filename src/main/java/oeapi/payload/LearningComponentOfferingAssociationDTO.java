package oeapi.payload;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * OEAPI v6 LearningComponentOfferingAssociation.
 */
public class LearningComponentOfferingAssociationDTO extends OfferingAssociationDTO {

    @JsonProperty("learningComponentOfferingId")
    public String learningComponentOfferingId() {
        return getOfferingId();
    }

    @JsonProperty("learningComponentOfferingId")
    public void learningComponentOfferingId(String id) {
        setOfferingId(id);
    }
}
