package oeapi.controller;


import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import oeapi.oeapiException;

/**
 *
 * @author itziar.urrutia
 */

@ControllerAdvice
public class oeapiAplicationExceptionHandler {

    private static final Map<HttpStatus, String> httpStatusToContentType = new HashMap<>();

    {
        httpStatusToContentType.put(HttpStatus.OK, "application/json");
        httpStatusToContentType.put(HttpStatus.CREATED, "application/json");
        httpStatusToContentType.put(HttpStatus.BAD_REQUEST, "application/problem+json");
        httpStatusToContentType.put(HttpStatus.NOT_FOUND, "application/problem+json");
        httpStatusToContentType.put(HttpStatus.INTERNAL_SERVER_ERROR, "application/problem+json");
        httpStatusToContentType.put(HttpStatus.ACCEPTED, "application/json");
        httpStatusToContentType.put(HttpStatus.NO_CONTENT, "application/json");
        httpStatusToContentType.put(HttpStatus.FOUND, "application/json");

        // Add more mappings as needed
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return badRequestResponse("Failed in validation constraints", errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleJsonValidationException(ValidationException ex) {
        List<String> errors = ex.getAllMessages();
        return badRequestResponse("JSON validation error", errors);
    }


    @ExceptionHandler(oeapiException.class)
    public ResponseEntity<?> handleOoapiUnitaException(oeapiException ex) {
        return response(ex.getStatus(), ex.getTitle(), ex.getDetail());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<?> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        return badRequestResponse("Can not update or delete entity", ex.getMessage());
    }

    private ResponseEntity<Object> response(HttpStatus status, String title, Object details) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, httpStatusToContentType.get(status));

        Map<String, Object> body =
            Map.of("status", status.value(),
                   "title", title,
                   "details", details);

        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> badRequestResponse(String title, Object detail) {
        return response(HttpStatus.BAD_REQUEST, title, detail);
    }
}
