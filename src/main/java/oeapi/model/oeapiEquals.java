package oeapi.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import static oeapi.oeapiUtils.ooapiObjectMapper;

/**
 * Implement .equals and .hashCode based on the db serialized values.
 */
abstract class oeapiEquals {
    private ObjectMapper objectMapper = ooapiObjectMapper();

    @Override
    public boolean equals(Object that) {
        try {
            return this == that ||
                    (that != null &&
                            getClass() == that.getClass() &&
                            objectMapper.writeValueAsString(this).equals(objectMapper.writeValueAsString(that)));
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return objectMapper.writeValueAsString(this).hashCode();
        } catch (Exception ex) {
            return super.hashCode();
        }
    }
}
