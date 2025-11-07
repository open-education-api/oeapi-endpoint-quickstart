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
public class oeapiPersonRequestParam extends oeapiRequestParam {

    /**
     * @return the affiliations
     */
    private static final List<String> validSortAttributes = Arrays.asList("personId", "givenName", "surname", "displayName");
    //private static final List<String> validSortAttributes = Arrays.asList("displayName");
    private final String[] sortDefault = {"displayName"};

    public String getAffiliations() {
        return affiliations;
    }

    /**
     * @param affiliations the affiliations to set
     */
    public void setAffiliations(String affiliations) {
        this.affiliations = affiliations;
    }

    private String affiliations;

    public Map.Entry<String, String> getFilter() {
        if (getAffiliations() != null) {
            return new AbstractMap.SimpleEntry<>("affiliations", getAffiliations());
        }
        return super.getFilter();
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes, sortDefault); // Pass the valid sort attributes for Person
    }

}
