package oeapi.controller;

import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.controller.requestparameters.oeapiProgramRequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import oeapi.model.Course;
import oeapi.model.Program;
import oeapi.oeapiException;
import oeapi.service.CourseService;
import oeapi.service.ProgramService;
import oeapi.service.oeapiDTOServiceInterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import oeapi.payload.ProgramDTO;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/programs")
public class ProgramController extends oeapiDTOController<Program, ProgramDTO> implements oeapiDTOControllerInterface<Program, ProgramDTO> {

    @Autowired
    private ProgramService service;

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiProgramRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();

        if (filter == null) {
            return super.getAll(requestParam.toPageable(), service);
        }
        if (filter.getKey().equalsIgnoreCase("primaryCode")) {
            return super.getAllByPrimaryCode(filter.getValue(), requestParam.toPageable(), service);
        } else {
            return super.getAllByFieldValue(filter.getKey(), filter.getValue(), requestParam.toPageable(), service);
        }
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id, @RequestParam(required = false) String expand)
            throws JsonProcessingException {
        return super.get(id, expand, service);
    }

    @GetMapping(value = "/{id}/courses", produces = "application/json")
    public ResponseEntity<?> getCourses(@PathVariable String id, @ModelAttribute oeapiCourseRequestParam requestParam) {
        Optional<Program> existing = service.getById(id);
        if (existing.isPresent()) {
            return super.getResponse(requestParam.toPageable(), courseService.getCoursesByProgramId(id));
        }

        throw new oeapiException(HttpStatus.NOT_FOUND, "There are not courses for Id: " + id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Program program) {
        return super.create(program, service);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProgram(@PathVariable String id) {

        Optional<Program> existing = service.getById(id);

        if (existing.isPresent()) {
            service.delete(id);
            return ResponseEntity.ok().build();
        } else {
           throw new oeapiException(HttpStatus.NOT_FOUND, "Error delete program with Id: " + id);
        }
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByProgramsJSON(@RequestBody List<ProgramDTO> items) {
        return super.createByJSON(items, service);
    }

    @Override
    public ResponseEntity<?> createFromDTO(ProgramDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createFromDTO'");
    }

    @Override
    public ResponseEntity<?> updateFromDTO(String courseId, ProgramDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateFromDTO'");
    }
}
