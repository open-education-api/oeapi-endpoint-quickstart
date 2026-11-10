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
public class oeapiOrganisationRequestParam extends oeapiRequestParam {

    /**
     * @return the organisationType
     */
    public String getOrganisationType() {
        return organisationType;
    }

    /**
     * @param organisationType the organisationType to set
     */
    public void setOrganisationType(String organisationType) {
        this.organisationType = organisationType;
    }

    private String organisationType;

    private static final List<String> validSortAttributes = Arrays.asList("organisationId", "name");

    public Map.Entry<String, String> getFilter() {
        if (getOrganisationType() != null) {
            return new AbstractMap.SimpleEntry<>("organisationType", getOrganisationType());
        }
        return super.getFilter();
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }
}
