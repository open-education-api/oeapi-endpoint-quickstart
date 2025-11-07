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
public class oeapiAcademicSessionRequestParam extends oeapiRequestParam {

    private static final List<String> validSortAttributes = Arrays.asList("academicSessionId", "name", "startDate");

    /**
     * @return the academicSessionType
     */
    public String getAcademicSessionType() {
        return academicSessionType;
    }

    /**
     * @param academicSessionType the academicSessionType to set
     */
    public void setAcademicSessionType(String academicSessionType) {
        this.academicSessionType = academicSessionType;
    }

    private String academicSessionType;

    public Map.Entry<String, String> getFilter() {
        if (academicSessionType != null) {
            return new AbstractMap.SimpleEntry<>("academicSessionType", academicSessionType);
        }
        return super.getFilter();
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }

}
