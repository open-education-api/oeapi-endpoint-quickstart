package oeapi.model;

/**
 *
 * @author itziar.urrutia
 */
public class Enrollment {

    private String crohoCreboCode;
    private String name;
    private String phase;
    private String modeOfStudy;
    private String startDate;
    private String endDate;

    /**
     * @return the crohoCreboCode
     */
    public String getCrohoCreboCode() {
        return crohoCreboCode;
    }

    /**
     * @param crohoCreboCode the crohoCreboCode to set
     */
    public void setCrohoCreboCode(String crohoCreboCode) {
        this.crohoCreboCode = crohoCreboCode;
    }

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
     * @return the phase
     */
    public String getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * @return the modeOfStudy
     */
    public String getModeOfStudy() {
        return modeOfStudy;
    }

    /**
     * @param modeOfStudy the modeOfStudy to set
     */
    public void setModeOfStudy(String modeOfStudy) {
        this.modeOfStudy = modeOfStudy;
    }

    /**
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
