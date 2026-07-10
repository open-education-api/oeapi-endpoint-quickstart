package oeapi.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class Ext implements Serializable {
    private Map<String, Object> anyAttributes = new HashMap<>();

    @JsonAnySetter
    public void set(String name, Object value) {
        anyAttributes.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> get() {
        return anyAttributes;
    }
}
