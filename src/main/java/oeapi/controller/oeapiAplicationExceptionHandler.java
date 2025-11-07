package oeapi.controller;


import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import oeapi.oeapiException;
import oeapi.oeapiStatus;
import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;

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

    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("error", "Failed in validation constraints");
        responseBody.put("messages", errors);

        String contentType = httpStatusToContentType.get(HttpStatus.BAD_REQUEST);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        return new ResponseEntity<>(responseBody, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleJsonValidationException(ValidationException ex) {
        // Collect all validation errors
        //ex.getViolatedSchema().
        List<String> errors = ex.getAllMessages();

        // Build the response body
        Map<String, Object> responseBody = new HashMap<>();

        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("title", "JSON validation error");
        responseBody.put("detail", errors);

        String contentType = httpStatusToContentType.get(HttpStatus.BAD_REQUEST);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        return new ResponseEntity<>(responseBody, headers, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(oeapiException.class)
    public ResponseEntity<oeapiStatus> handleOoapiUnitaException(oeapiException ex, WebRequest request
    ) {

        oeapiStatus error;

        if (ex.getDetail() == null) {
            error = new oeapiStatus(String.valueOf(ex.getStatus().value()), ex.getTitle());
        } else {
            error = new oeapiStatus(String.valueOf(ex.getStatus().value()), ex.getTitle(), ex.getDetail());
        }

        String contentType = httpStatusToContentType.get(ex.getStatus());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/problem+json");
        return new ResponseEntity<>(error, headers, ex.getStatus());  //TDB Fine tune Status
    }
}
