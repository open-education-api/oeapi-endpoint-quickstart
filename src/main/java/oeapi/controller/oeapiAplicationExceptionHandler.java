package oeapi.controller;


import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import org.everit.json.schema.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import oeapi.OeapiMediaType;
import oeapi.oeapiException;
import oeapi.oeapiProblem;

/**
 * Turns exceptions into OEAPI v6 error responses: RFC 7807 Problem Details
 * served as application/problem+json with the real HTTP status.
 *
 * @author itziar.urrutia
 */

@ControllerAdvice
public class oeapiAplicationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<oeapiProblem> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return response(HttpStatus.BAD_REQUEST, "Failed in validation constraints", String.join("; ", errors), request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<oeapiProblem> handleJsonValidationException(ValidationException ex, WebRequest request) {
        return response(HttpStatus.BAD_REQUEST, "JSON validation error", String.join("; ", ex.getAllMessages()), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<oeapiProblem> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Invalid request parameters", ex.getMessage(), request);
    }

    @ExceptionHandler(oeapiException.class)
    public ResponseEntity<oeapiProblem> handleOoapiUnitaException(oeapiException ex, WebRequest request) {
        HttpStatus status = ex.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : ex.getStatus();
        String title = ex.getTitle() == null ? ex.getMessage() : ex.getTitle();
        return response(status, title, ex.getDetail(), request);
    }

    /** OEAPI v6 answers 409 Conflict when a write clashes with the current state of the data. */
    @ExceptionHandler({SQLIntegrityConstraintViolationException.class, DataIntegrityViolationException.class})
    public ResponseEntity<oeapiProblem> handleSQLIntegrityConstraintViolation(Exception ex, WebRequest request) {
        return response(HttpStatus.CONFLICT, "Can not update or delete entity", ex.getMessage(), request);
    }

    static ResponseEntity<oeapiProblem> response(HttpStatus status, String title, String detail, WebRequest request) {
        oeapiProblem problem = new oeapiProblem(status.value(), title == null ? status.getReasonPhrase() : title, detail);

        if (request instanceof ServletWebRequest servletRequest) {
            problem.setInstance(servletRequest.getRequest().getRequestURL().toString());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, OeapiMediaType.PROBLEM_JSON);

        return new ResponseEntity<>(problem, headers, status);
    }
}
