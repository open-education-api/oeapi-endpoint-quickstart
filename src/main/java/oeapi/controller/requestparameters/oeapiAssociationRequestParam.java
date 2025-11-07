/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
public class oeapiAssociationRequestParam extends oeapiRequestParam {

    private static final List<String> validSortAttributes = Arrays.asList("associationId");

    private String state;
    private String resultState;
    private String role;
    private String associationType;
    private final String[] sortDefault = {"associationId"};
    
    
    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the associationType
     */
    public String getAssociationType() {
        return associationType;
    }

    /**
     * @param associationType the associationType to set
     */
    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the resultState
     */
    public String getResultState() {
        return resultState;
    }

    /**
     * @param resultState the resultState to set
     */
    public void setResultState(String resultState) {
        this.resultState = resultState;
    }

    public Map.Entry<String, String> getFilter() {
        if (state != null) {
            return new AbstractMap.SimpleEntry<>("state", state);
        }
        if (resultState != null) {
            return new AbstractMap.SimpleEntry<>("resultState", resultState);
        }
        if (role != null) {
            return new AbstractMap.SimpleEntry<>("role", role);
        }
        if (associationType != null) {
            return new AbstractMap.SimpleEntry<>("associationType", role);
        }
        return super.getFilter();
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes, sortDefault); // Pass the valid sort attributes for Person
    }

}
