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
public class oeapiEducationSpecificationRequestParam extends oeapiRequestParam {

    private static final List<String> validSortAttributes = Arrays.asList("educationSpecificationType", "name", "primaryCode");

    /**
     * @return the educationSpecificationType
     */
    public String getEducationSpecificationType() {
        return educationSpecificationType;
    }

    /**
     * @param educationSpecificationType the educationSpecificationType to set
     */
    public void setEducationSpecificationType(String educationSpecificationType) {
        this.educationSpecificationType = educationSpecificationType;
    }

    private String educationSpecificationType;

    public Map.Entry<String, String> getFilter() {
        if (educationSpecificationType != null) {
            return new AbstractMap.SimpleEntry<>("educationSpecificationType", educationSpecificationType);
        }
        return super.getFilter();
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }

}
