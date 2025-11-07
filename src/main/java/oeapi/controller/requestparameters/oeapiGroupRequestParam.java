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
public class oeapiGroupRequestParam extends oeapiRequestParam {

    private static final List<String> validSortAttributes = Arrays.asList("groupId", "name", "startDate");

    /**
     * @return the groupType
     */
    public String getGroupType() {
        return groupType;
    }

    /**
     * @param groupType the groupType to set
     */
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    private String groupType;

    public Map.Entry<String, String> getFilter() {
        if (getGroupType() != null) {
            return new AbstractMap.SimpleEntry<>("groupType", getGroupType());
        }
        return super.getFilter();
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }

}
