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
public class oeapiNewsFeedRequestParam extends oeapiRequestParam {

    private static final List<String> validSortAttributes = Arrays.asList("newsFeedId", "name");

    /**
     * @return the newsFeedType
     */
    public String getNewsFeedType() {
        return newsFeedType;
    }

    /**
     * @param newsFeedType the newsFeedType to set
     */
    public void setNewsFeedType(String newsFeedType) {
        this.newsFeedType = newsFeedType;
    }

    private String newsFeedType;

    public Map.Entry<String, String> getFilter() {
        if (newsFeedType != null) {
            return new AbstractMap.SimpleEntry<>("newsFeedType", newsFeedType);
        }
        return null;
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }

}
