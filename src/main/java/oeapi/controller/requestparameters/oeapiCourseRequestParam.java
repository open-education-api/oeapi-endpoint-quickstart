package oeapi.controller.requestparameters;

import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiCourseRequestParam extends oeapiRequestParam {

    private String level;
    private String teachingLanguage;
    private String fieldsOfStudy;

    private static final List<String> validSortAttributes = Arrays.asList("courseId", "name");

    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
        this.level = level;
    }

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

    public Map.Entry<String, String> getFilter() {
        return this.getFilter(true);
    }

    public Map.Entry<String, String> getFilter(boolean hasPrimaryCodeFilter) {
        if (getLevel() != null) {
            return new AbstractMap.SimpleEntry<>("level", getLevel());
        }
        if (getTeachingLanguage() != null) {
            return new AbstractMap.SimpleEntry<>("teachingLanguage", getTeachingLanguage());
        }
        if (getFieldsOfStudy() != null) {
            return new AbstractMap.SimpleEntry<>("fieldsOfStudy", getFieldsOfStudy());
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

}
