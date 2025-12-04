package oeapi.controller;

import oeapi.controller.requestparameters.oeapiRequestParam;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import oeapi.model.Course;
import oeapi.oeapiException;
import oeapi.oeapiObjectsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.ObjectError;
import oeapi.service.oeapiDTOServiceInterface;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiDTOController<T, S> {

    Logger logger = LoggerFactory.getLogger(oeapiDTOController.class);

    @Autowired
    private oeapiObjectsValidator validator;

    public void Validate(T object, Errors errors) {
        validator.validate(object, errors);
    }

    public void ValidateDTO(S object, Errors errors) {
        validator.validate(object, errors);
    }

    protected ResponseEntity<?> NotFound(String id) {

        return ResponseEntity.notFound().build();
    }

    protected ResponseEntity<?> getResponse(oeapiRequestParam requestParam, List<?> items) {
        // Convert pageNumber (1-based index) to 0-based index for Pageable
        //            Pageable pageable = PageRequest.of(requestParam.getPageNumber() - 1, requestParam.getPageSize());
        Pageable pageable = PageRequest.of(0, 100);

        oeapiResponse response = new oeapiResponse(items, pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    protected ResponseEntity<?> getResponse(Pageable pageable, List<?> items) {
        // Convert pageNumber (1-based index) to 0-based index for Pageable
        //            Pageable pageable = PageRequest.of(requestParam.getPageNumber() - 1, requestParam.getPageSize());

        oeapiResponse response = new oeapiResponse(items, pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    protected ResponseEntity<?> getResponse(Page<?> pages) {
        oeapiResponse response = new oeapiResponse(pages);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<?> getAll(Map.Entry<String, String> filter, Pageable pageable, oeapiDTOServiceInterface<T, S> service) {

        if (filter == null) {
            return this.getAll(pageable, service);
        }

        if (filter.getKey().equals("primaryCode")) {
            return this.getAllByPrimaryCode(filter.getValue(), pageable, service);
        } else {
            return this.getAllByFieldValue(filter.getKey(), filter.getValue(), pageable, service);
        }

    }

    public ResponseEntity<?> getAll(Pageable pageable, oeapiDTOServiceInterface<T, S> service) {

        // Convert pageNumber (1-based index) to 0-based index for Pageable
        //Pageable pageable = PageRequest.of(requestParam.getPageNumber() - 1, requestParam.getPageSize());
        Page<T> pages = service.getAll(pageable);
        //pages = service.getAll(pageable);
        return this.getResponse(service.toDTOPages(pages));

    }

    public ResponseEntity<?> getAll(oeapiRequestParam requestParam, oeapiDTOServiceInterface<T, S> service) {

        Pageable pageable = PageRequest.of(0, 100);
        return this.getAll(pageable, service);

    }

    protected ResponseEntity<?> getAllByPrimaryCode(String primaryCode, oeapiRequestParam requestParam, oeapiDTOServiceInterface<T, S> service) {

        Pageable pageable = PageRequest.of(0, 100);
        return this.getAllByPrimaryCode(primaryCode, pageable, service);

    }

    protected ResponseEntity<?> getAllByPrimaryCode(String primaryCode, Pageable pageable, oeapiDTOServiceInterface<T, S> service) {

        // Convert pageNumber (1-based index) to 0-based index for Pageable
        //Pageable pageable = PageRequest.of(requestParam.getPageNumber() - 1, requestParam.getPageSize());
        //Page<T> pages = service.getAll(pageable);
        //pages = service.getAll(pageable);
        Page<T> pages = service.getByPrimaryCode(primaryCode, pageable);
        return this.getResponse(service.toDTOPages(pages));

    }

    protected ResponseEntity<?> getAllByFieldValue(String field, String value, oeapiRequestParam requestParam, oeapiDTOServiceInterface<T, S> service) {

        Pageable pageable = PageRequest.of(0, 100);
        return this.getAllByFieldValue(field, value, pageable, service);

    }

    protected ResponseEntity<?> getAllByFieldValue(String field, String value, Pageable pageable, oeapiDTOServiceInterface<T, S> service) {

        Page<T> pages = service.getByField(field, value, pageable);
        return this.getResponse(service.toDTOPages(pages));

    }

    public ResponseEntity<?> get(String id, oeapiDTOServiceInterface<T, S> service) {
        Optional<T> p = service.getById(id);
        if (!p.isPresent()) {
            //return ResponseEntity.badRequest().body("Error: " + id + " not found");
             throw new oeapiException(HttpStatus.NOT_FOUND, "There is not such info for Id: " + id);
        } else {
            T obj = p.get();
            //return ResponseEntity.ok("Program created successfully: " + id);
            return ResponseEntity.ok(service.toDTO(obj));
        }

    }

    public ResponseEntity<?> get(String id, String expand, oeapiDTOServiceInterface<T, S> service) {
        Optional<T> p = service.getById(id);
        if (!p.isPresent()) {
            //return ResponseEntity.badRequest().body("Error: " + id + " not found");
             throw new oeapiException(HttpStatus.NOT_FOUND, "There is not such info for Id: " + id);
        } else {
            T obj = p.get();
            //return ResponseEntity.ok("Program created successfully: " + id);
            return ResponseEntity.ok(service.toDTOString(obj, expand));
        }

    }

    public ResponseEntity<?> update(@RequestBody @Valid T requestBody, oeapiDTOServiceInterface<T, S> service) {

        ResponseEntity finalResponse = null;
        Errors errors = new BeanPropertyBindingResult(requestBody, requestBody.getClass().getName().toLowerCase());
        T updated;
        //validator.validate(program,errors)
        // Just for debugging, see how the parsed JSON looks like
        logger.debug("ooapiDTOController (super): Entering Update with this JSON: " + requestBody);

        logger.debug("ooapiDTOController (super): Going to validate...");
        this.Validate(requestBody, errors);

        if (errors.hasErrors()) {
            finalResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Error JSON validation", errors.getAllErrors());  // TDB Fine tune status
            return ResponseEntity.badRequest().body(errors.getAllErrors());
        }

        try {
            logger.debug("ooapiDTOController (super): Validated and ready to call service to update BD");
            updated = service.update(requestBody);
            finalResponse = ResponseEntity.ok(service.toDTO(updated));
            //finalResponse = ResponseEntity.ok(updated);
        } catch (oeapiException ooapiEx) {
            logger.error("ooapiDTOController (super): " + ooapiEx.getTitle());
            finalResponse = createErrorResponse(HttpStatus.NOT_FOUND, ooapiEx.getTitle(), ooapiEx.getDetail());  // TDB Fine tune status
        } catch (Exception ex) {
            logger.error("ooapiDTOController (super): " + ex);
            finalResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Not handled Exception", ex.getLocalizedMessage());   // TDB Fine tune status
        }

        logger.debug("ooapiController (super): BD Updated");

        return finalResponse;
    }

    public ResponseEntity<?> createOrUpdate(String id, @RequestBody @Valid S dto, oeapiDTOServiceInterface<T, S> service) {
        try {
            Optional<T> existing = service.getById(id);
            if (existing.isPresent()) {
                return this.updateDTO(dto, service);
            } else {
                return this.createDTO(dto, service);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    public ResponseEntity<?> updateDTO(@RequestBody @Valid S requestBody, oeapiDTOServiceInterface<T, S> service) {
        ResponseEntity finalResponse = null;
        T updated;
        //validator.validate(program,errors)
        // Just for debugging, see how the parsed JSON looks like

        try {
            logger.debug("ooapiDTOController (super): Validated and ready to call service to update BD");
            updated = service.update(service.toEntity(requestBody));
            finalResponse = ResponseEntity.ok(service.toDTO(updated));
            //finalResponse = ResponseEntity.ok(updated);
        } catch (oeapiException ooapiEx) {
            logger.error("ooapiDTOController (super): " + ooapiEx.getTitle());
            finalResponse = createErrorResponse(HttpStatus.NOT_FOUND, ooapiEx.getTitle(), ooapiEx.getDetail());  // TDB Fine tune status
        } catch (Exception ex) {
            logger.error("ooapiDTOController (super): " + ex);
            finalResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Not handled Exception", ex.getLocalizedMessage());   // TDB Fine tune status
        }

        logger.debug("ooapiDTOController (super): BD Updated");

        return finalResponse;
    }

    public ResponseEntity<?> create(@RequestBody @Valid T requestBody, oeapiDTOServiceInterface<T, S> service) {
        //public ResponseEntity<Program> create(@RequestBody Map<String, Object> program) {

        ResponseEntity finalResponse = null;
        Errors errors = new BeanPropertyBindingResult(requestBody, requestBody.getClass().getName().toLowerCase());
        T created;
        //validator.validate(requestBody, errors);
        // Just for debugging, see how the parsed JSON looks like
        logger.debug("ooapiDTOController: " + requestBody);

        this.Validate(requestBody, errors);
        if (errors.hasErrors()) {
            finalResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Error JSON validation", errors.getAllErrors());  // TDB Fine tune status
            //return ResponseEntity.badRequest().body(errors.getAllErrors());
        }
        try {
            created = service.create(requestBody);
            finalResponse = ResponseEntity.ok(service.toDTO(created));
        } catch (oeapiException ooapiEx) {
            logger.error("ooapiDTOController ooapiException: " + ooapiEx.getTitle());
            finalResponse = createErrorResponse(ooapiEx.getStatus(), ooapiEx.getTitle(), ooapiEx.getDetail());  // TDB Fine tune status
        } catch (Exception ex) {
            logger.error("ooapiDTOController Exception: " + ex);
            finalResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Not handled Exception", ex.getLocalizedMessage());   // TDB Fine tune status
        }
        return finalResponse;
    }

    public ResponseEntity<?> createDTO(@RequestBody @Valid S requestBody, oeapiDTOServiceInterface<T, S> service) {
        //public ResponseEntity<Program> create(@RequestBody Map<String, Object> program) {

        ResponseEntity finalResponse = null;
        Errors errors = new BeanPropertyBindingResult(requestBody, requestBody.getClass().getName().toLowerCase());
        T created;
        //validator.validate(requestBody, errors);
        // Just for debugging, see how the parsed JSON looks like
        logger.debug("ooapiController: " + requestBody);

        this.ValidateDTO(requestBody, errors);
        if (errors.hasErrors()) {
            finalResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Error JSON validation", errors.getAllErrors());  // TDB Fine tune status
            //return ResponseEntity.badRequest().body(errors.getAllErrors());
        }
        try {
            created = service.create(service.toEntity(requestBody));
            finalResponse = ResponseEntity.ok(service.toDTO(created));
        } catch (oeapiException ooapiEx) {
            logger.error("ooapiController: " + ooapiEx.getTitle());
            finalResponse = createErrorResponse(ooapiEx.getStatus(), ooapiEx.getTitle(), ooapiEx.getDetail());  // TDB Fine tune status
        } catch (Exception ex) {
            logger.error("ooapiController: " + ex);
            finalResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Not handled Exception", ex.getLocalizedMessage());   // TDB Fine tune status
        }
        return finalResponse;
    }

    /*
    public ResponseEntity<String> createByJSON(List<T> items, oeapiDTOServiceInterface<T, S> service) {

        // Check if this entry point allows updates using REST or is it read-only
        //if (!allowRestToModify) {
        //    throw new oeapiException(HttpStatus.NOT_FOUND, "This OOAPI entry point does not allow updates using REST");
        //}

        ResponseEntity itemResponse;
        StringBuilder textResult = new StringBuilder();

        int countProcessed = 0;
        int countWithError = 0;

        for (T item : items) {
            itemResponse = this.create(item, service);

            textResult.append("\n" + itemResponse.getBody());

            countProcessed++;

            if (itemResponse.getStatusCodeValue() > 299) {
                countWithError++;
            }
        }

        textResult.insert(0, "Objects processed: " + countProcessed + ", Courses with error: " + countWithError + ", Full log: ");

        return new ResponseEntity<>(textResult.toString(), HttpStatus.ACCEPTED);

    }
     */
    public ResponseEntity<String> createByJSON(List<S> items, oeapiDTOServiceInterface<T, S> service) {

        /* Check if this entry point allows updates using REST or is it read-only
        if (!allowRestToModify) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "This OOAPI entry point does not allow updates using REST");
        }
         */
        ResponseEntity itemResponse;
        StringBuilder textResult = new StringBuilder();

        int countProcessed = 0;
        int countWithError = 0;

        for (S item : items) {
            itemResponse = this.createDTO(item, service);

            textResult.append("\n" + itemResponse.getBody());

            countProcessed++;

            if (itemResponse.getStatusCodeValue() > 299) {
                countWithError++;
            }
        }

        textResult.insert(0, "Objects processed: " + countProcessed + ", Courses with error: " + countWithError + ", Full log: ");

        return new ResponseEntity<>(textResult.toString(), HttpStatus.ACCEPTED);

    }

    public ResponseEntity<ObjectNode> createErrorResponse(HttpStatus status, String title, String detail) {
        ObjectNode errorNode = JsonNodeFactory.instance.objectNode();
        errorNode.put("status", status.value());
        errorNode.put("title", title);

        if (detail != null) {
            errorNode.put("detail", detail);
        }
        //throw new oeapiException(status, title, detail);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/problem+json");

        return new ResponseEntity<>(errorNode, headers, status);  //TDB Fine tune Status

    }

    public ResponseEntity<ObjectNode> createErrorResponse(HttpStatus status, String title, List<ObjectError> errors) {
        ObjectNode errorNode = JsonNodeFactory.instance.objectNode();
        errorNode.put("status", status.value());
        errorNode.put("title", title);
        String detail = "";
        for (ObjectError e : errors) {
            detail += e.getDefaultMessage() + "\n";
        }
        errorNode.put("detail", detail);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/problem+json");

        return new ResponseEntity<>(errorNode, headers, status);
        //throw new oeapiException(status, title, detail);

    }

}
