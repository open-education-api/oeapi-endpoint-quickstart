package oeapi.controller;

import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.controller.requestparameters.oeapiProgramRequestParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import oeapi.model.Course;
import oeapi.model.Offering;
import oeapi.model.Program;
import oeapi.model.ProgramOffering;
import oeapi.oeapiException;
import oeapi.payload.ProgramOfferingDTO;
import oeapi.service.CourseService;
import oeapi.service.OfferingService;
import oeapi.service.ProgramService;
import oeapi.service.oeapiEnumConversionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private OfferingService offeringService;

    @Autowired
    private oeapiEnumConversionService enumService;

    @Value("${ooapi.config.autoCreateOfferingIfNotExists:false}")
    private boolean defaultAutoCreateOfferingIfNotExists;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiProgramRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();

        if (filter == null) {
            return super.getAll(requestParam.toPageable(), service);
        }
        if (filter.getKey().equalsIgnoreCase("primaryCode")) {
            return super.getAllByPrimaryCode(filter.getValue(), requestParam.toPageable(), service);
        }

        return super.getAllByFieldValue(filter.getKey(), filter.getValue(), requestParam.toPageable(), service);
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

    @GetMapping(value = "/{id}/offerings", produces = "application/json")
    public ResponseEntity<?> getOfferings(@PathVariable String id, @ModelAttribute oeapiOfferingRequestParam requestParam) {

        Optional<Program> existing = service.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are no program-offerings for Id: " + id);
        }
        List<ProgramOffering> programOfferings = offeringService.getByProgramId(id);

        // If there is no ProgramOffering and Autocreate is on, return a basic autogen offering
        if (programOfferings.isEmpty()) {
            List<Offering> offerings = new ArrayList<>();
            if (defaultAutoCreateOfferingIfNotExists) {
                offerings.add(offeringService.autoGenerateBasicItem(id));
            }
            return super.getResponse(requestParam, offerings);
        }
        oeapiDTOMapper<ProgramOffering, ProgramOfferingDTO> mapper =
                new oeapiDTOMapper<ProgramOffering, ProgramOfferingDTO>(ProgramOffering.class,
                        ProgramOfferingDTO.class,
                        enumService,
                        Arrays.asList());

        List<ProgramOfferingDTO> dtos = mapper.toDTOList(programOfferings);
        Pageable pageable = requestParam.toPageable();
        oeapiResponse<ProgramOfferingDTO> response = new oeapiResponse<ProgramOfferingDTO>(dtos, pageable);

        return new ResponseEntity<>(response, HttpStatus.OK);
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
        throw new oeapiException(HttpStatus.NOT_IMPLEMENTED,"Method 'updateFromDTO' is not yet implemented");
    }

    @Override
    public ResponseEntity<?> updateFromDTO(String courseId, ProgramDTO dto) {
        throw new oeapiException(HttpStatus.NOT_IMPLEMENTED,"Method 'updateFromDTO' is not yet implemented");
    }
}
