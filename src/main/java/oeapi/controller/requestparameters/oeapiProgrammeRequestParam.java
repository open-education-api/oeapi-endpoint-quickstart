package oeapi.controller.requestparameters;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiProgrammeRequestParam extends oeapiRequestParam {

    private String programmeType;
    private String qualificationAwarded;
    private String levelOfQualification;
    private String fieldsOfStudy;
    private String teachingLanguage;

    private static final List<String> validSortAttributes = Arrays.asList("programmeId", "name");

    /**
     * @return the teachingLanguage
     */
    public String getTeachingLanguage() {
        return teachingLanguage;
    }

    /**
     * @param teachingLanguage the teachingLanguage to set
     */
    public void setTeachingLanguage(String teachingLanguage) {
        this.teachingLanguage = teachingLanguage;
    }

    /**
     * @return the programmeType
     */
    public String getProgrammeType() {
        return programmeType;
    }

    /**
     * @param programmeType the programmeType to set
     */
    public void setProgrammeType(String programmeType) {
        this.programmeType = programmeType;
    }

    /**
     * @return the qualificationAwarded
     */
    public String getQualificationAwarded() {
        return qualificationAwarded;
    }

    /**
     * @param qualificationAwarded the qualificationAwarded to set
     */
    public void setQualificationAwarded(String qualificationAwarded) {
        this.qualificationAwarded = qualificationAwarded;
    }

    /**
     * @return the levelOfQualification
     */
    public String getLevelOfQualification() {
        return levelOfQualification;
    }

    /**
     * @param levelOfQualification the levelOfQualification to set
     */
    public void setLevelOfQualification(String levelOfQualification) {
        this.levelOfQualification = levelOfQualification;
    }

    /**
     * @return the fieldsOfStudy
     */
    public String getFieldsOfStudy() {
        return fieldsOfStudy;
    }

    /**
     * @param fieldsOfStudy the fieldsOfStudy to set
     */
    public void setFieldsOfStudy(String fieldsOfStudy) {
        this.fieldsOfStudy = fieldsOfStudy;
    }

    @Override
    public Map.Entry<String, String> getFilter() {
        return this.getFilter(true);
    }

    public Map.Entry<String, String> getFilter(boolean hasPrimaryCodeFilter) {
        if (programmeType != null) {
            return new AbstractMap.SimpleEntry<>("programmeType", programmeType);
        }
        if (qualificationAwarded != null) {
            return new AbstractMap.SimpleEntry<>("qualificationAwarded", qualificationAwarded);
        }
        if (levelOfQualification != null) {
            return new AbstractMap.SimpleEntry<>("levelOfQualification", levelOfQualification);
        }
        if (fieldsOfStudy != null) {
            return new AbstractMap.SimpleEntry<>("fieldsOfStudy", fieldsOfStudy);
        }
        if (teachingLanguage != null) {
            return new AbstractMap.SimpleEntry<>("teachingLanguagesContaining", teachingLanguage);
        }
        if (hasPrimaryCodeFilter) {
            return super.getFilter();
        } else {
            return null;
        }
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }
}
