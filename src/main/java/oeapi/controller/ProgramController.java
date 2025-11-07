package oeapi.controller;

import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.controller.requestparameters.oeapiProgramRequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import oeapi.model.Program;
import oeapi.oeapiException;
import oeapi.service.CourseService;
import oeapi.service.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/programs")
public class ProgramController extends oeapiController<Program> {

    /*
    @Autowired
    private ooapiObjectsValidator validator;
     */
    @Autowired
    private ProgramService service;

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiProgramRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();

        if (filter == null) {
            return super.getAll(requestParam.toPageable(), service);
            //return super.getAllByPrimaryCode(requestParam.getPrimaryCode(), requestParam, service)
        }
        if (filter.getKey() == "primaryCode") {
            return super.getAllByPrimaryCode(filter.getValue(), requestParam.toPageable(), service);
        } else {
            return super.getAllByFieldValue(filter.getKey(), filter.getValue(), requestParam.toPageable(), service);
        }
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);

    }

    //V6
    @GetMapping(value = "/{id}/program-offerings", produces = "application/json")
    public ResponseEntity<?> offerings(@PathVariable String id, oeapiOfferingRequestParam requestParam) {

        Optional<Program> existing = service.getById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Program program = existing.get();

        // Pending
        return super.getResponse(requestParam.toPageable(), new ArrayList());
    }

    @GetMapping(value = "/{id}/programs", produces = "application/json")
    public ResponseEntity<?> getPrograms(@PathVariable String id, @ModelAttribute oeapiProgramRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter(false);
        Optional<Program> existing = service.getById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Program program = existing.get();
        return super.getResponse(requestParam.toPageable(), new ArrayList());
    }

    @GetMapping(value = "/{id}/courses", produces = "application/json")
    public ResponseEntity<?> getCourses(@PathVariable String id, @ModelAttribute oeapiCourseRequestParam requestParam) {
        //public ResponseEntity<?> getCourses(@PathVariable String id, @RequestParam ooapiRequestParam requestParam) {

        //public ResponseEntity<?> getCourses(@PathVariable String id, @RequestParam ooapiRequestParam requestParam) {
        //Pageable pageable = PageRequest.of(requestParam.getPageNumber(), requestParam.getPageSize());
        Map.Entry<String, String> filter = requestParam.getFilter(false);
        Optional<Program> existing = service.getById(id);
        if (existing.isPresent()) {

            return super.getResponse(requestParam.toPageable(), courseService.getCoursesByProgramId(id));

        }
        //return super.NotFound(id);
        throw new oeapiException(HttpStatus.NOT_FOUND, "There are not courses for Id: " + id);

        
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Program program) {

        return super.create(program, service);

    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<Program> items) {

        /*
        /* Check if this entry point allows updates using REST or is it read-only
        if (!allowRestToModify) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "This OOAPI entry point does not allow updates using REST");
        }
         */
        return super.createByJSON(items, service);

    }
}
