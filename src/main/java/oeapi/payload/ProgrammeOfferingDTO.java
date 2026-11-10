package oeapi.payload;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import oeapi.model.Programme;

/**
 * OEAPI v6 ProgrammeOffering.
 *
 * @author itziar.urrutia
 */
@JsonPropertyOrder({"programmeOfferingId", "primaryCode", "programmeId"})
public class ProgrammeOfferingDTO extends OfferingDTO {

    @JsonProperty("programmeId")
    private String programmeId;

    @JsonIgnore
    @oeapiDTOExpandable
    public Programme programme;

    @JsonProperty("flexibleEntryPeriodStartDateTime")
    private OffsetDateTime flexibleEntryPeriodStart;

    @JsonProperty("flexibleEntryPeriodEndDateTime")
    private OffsetDateTime flexibleEntryPeriodEnd;

    @JsonProperty("programmeOfferingId")
    // Not named getProgrammeOfferingId(): ModelMapper must not see a second candidate for offeringId
    public String programmeOfferingId() {
        return getOfferingId();
    }

    @JsonProperty("programmeOfferingId")
    public void programmeOfferingId(String id) {
        setOfferingId(id);
    }

    @JsonIgnore
    public Programme getProgramme() {
        return programme;
    }

    @JsonIgnore
    public void setProgramme(Programme programme) {
        this.programme = programme;
        this.programmeId = programme == null ? null : programme.getProgrammeId();
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
        if (programmeId == null) {
            this.programme = null;
        } else if (this.programme == null || !programmeId.equals(this.programme.getProgrammeId())) {
            this.programme = new Programme(programmeId);
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
            return (programme == null) ? null : programme.getTeachingLanguages();
        }
        return super.getTeachingLanguages();
    }
}
