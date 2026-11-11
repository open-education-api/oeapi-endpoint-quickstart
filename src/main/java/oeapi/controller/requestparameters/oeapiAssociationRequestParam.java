package oeapi.controller.requestparameters;

import java.util.List;
import java.util.stream.Collectors;

import oeapi.model.OfferingAssociation;

/**
 * Query parameters of the OEAPI v6 association lists (role, state, resultState).
 */
public class oeapiAssociationRequestParam extends oeapiRequestParam {

    private String role;
    private String state;
    private String resultState;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getResultState() {
        return resultState;
    }

    public void setResultState(String resultState) {
        this.resultState = resultState;
    }

    /** Keeps the associations that match the role, state and resultState filters. */
    public List<OfferingAssociation> filter(List<OfferingAssociation> associations) {
        return associations.stream()
                .filter(a -> role == null || role.equals(a.getRole()))
                .filter(a -> state == null || state.equals(a.getState()))
                .filter(a -> resultState == null
                        || (a.getResult() != null && resultState.equals(a.getResult().getState())))
                .collect(Collectors.toList());
    }
}
