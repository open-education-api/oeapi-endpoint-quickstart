package oeapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import oeapi.controller.requestparameters.oeapiRequestParam;

/**
 * Builds the paginated responses of the v6 offering sub-resources
 * (/organisations/{id}/course-offerings, /academic-sessions/{id}/programme-offerings...).
 */
public final class oeapiOfferingLists {

    private oeapiOfferingLists() {
    }

    public static <D> ResponseEntity<?> page(oeapiRequestParam requestParam, List<D> dtos) {
        return new ResponseEntity<>(new oeapiResponse<D>(dtos, requestParam.toPageable()), HttpStatus.OK);
    }
}
