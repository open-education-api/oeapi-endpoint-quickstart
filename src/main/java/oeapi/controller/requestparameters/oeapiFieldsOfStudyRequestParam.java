package oeapi.controller.requestparameters;

import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiFieldsOfStudyRequestParam extends oeapiRequestParam {

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

    private String level;

    private static final List<String> validSortAttributes = Arrays.asList("fieldsOfStudyId");
    private final String[] sortDefault = {"fieldsOfStudyId"};

    public Map.Entry<String, String> getFilter() {
        if (getLevel() != null) {
            return new AbstractMap.SimpleEntry<>("level", getLevel());
        }
        return null;
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes, sortDefault);
    }
}
