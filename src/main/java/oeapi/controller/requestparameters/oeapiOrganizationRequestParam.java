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
public class oeapiOrganizationRequestParam extends oeapiRequestParam {

    /**
     * @return the organizationType
     */
    public String getOrganizationType() {
        return organizationType;
    }

    /**
     * @param organizationType the organizationType to set
     */
    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    private String organizationType;

    private static final List<String> validSortAttributes = Arrays.asList("organizationId", "name");

    public Map.Entry<String, String> getFilter() {
        if (getOrganizationType() != null) {
            return new AbstractMap.SimpleEntry<>("organizationType", getOrganizationType());
        }
        return super.getFilter();
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }
}
