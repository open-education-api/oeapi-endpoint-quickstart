package oeapi.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author itziar.urrutia
 */
public class Alliance {

    private Map<String, Object> otherAttributes = new HashMap<>();
    
    private String name;
     
    // Allow any attributes, the ones not explicity specified go into the map   
    
    @JsonAnySetter
    public void set(String name, Object value) {
        otherAttributes.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> get() {
        return otherAttributes;
    }

}
