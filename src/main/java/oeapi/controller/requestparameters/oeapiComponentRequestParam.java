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
public class oeapiComponentRequestParam extends oeapiRequestParam {

    private static final List<String> validSortAttributes = Arrays.asList("groupId", "name", "startDate");
    
    private String componentType;
    private String teachingLanguage;    

    /**
     * @return the componentType
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * @param componentType the componentType to set
     */
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }


    public Map.Entry<String, String> getFilter() {
        if (componentType != null) {
            return new AbstractMap.SimpleEntry<>("componentType", componentType);
        }
        if (teachingLanguage != null) {
            return new AbstractMap.SimpleEntry<>("teachingLanguage", teachingLanguage);
        }
        return null;
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }

}
