package oeapi.controller;

import oeapi.controller.requestparameters.oeapiCourseRequestParam;
import oeapi.controller.requestparameters.oeapiOfferingRequestParam;
import oeapi.controller.requestparameters.oeapiProgrammeRequestParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.jackson.core.JacksonException;

import oeapi.model.Course;
import oeapi.model.Offering;
import oeapi.model.Programme;
import oeapi.model.ProgrammeOffering;
import oeapi.oeapiException;
import oeapi.payload.ProgrammeOfferingDTO;
import oeapi.service.CourseService;
import oeapi.service.OfferingService;
import oeapi.service.ProgrammeService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import oeapi.payload.ProgrammeDTO;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/programmes")
public class ProgrammeController extends oeapiDTOController<Programme, ProgrammeDTO> implements oeapiDTOControllerInterface<Programme, ProgrammeDTO> {

    @Autowired
    private ProgrammeService service;

    @Autowired
    private CourseService courseService;

    @Autowired
    private OfferingService offeringService;

    @Autowired
    private oeapiEnumConversionService enumService;

    @Value("${ooapi.config.autoCreateOfferingIfNotExists:false}")
    private boolean defaultAutoCreateOfferingIfNotExists;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute oeapiProgrammeRequestParam requestParam) {
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
            throws JacksonException {
        return super.get(id, expand, service);
    }

    @GetMapping(value = "/{id}/courses", produces = "application/json")
    public ResponseEntity<?> getCourses(@PathVariable String id, @ModelAttribute oeapiCourseRequestParam requestParam) {
        Optional<Programme> existing = service.getById(id);
        if (existing.isPresent()) {
            return super.getResponse(requestParam.toPageable(), courseService.toDTOList(courseService.getCoursesByProgrammeId(id)));
        }

        throw new oeapiException(HttpStatus.NOT_FOUND, "There are not courses for Id: " + id);
    }

    @GetMapping(value = "/{id}/programmes", produces = "application/json")
    public ResponseEntity<?> getChildProgrammes(@PathVariable String id, @ModelAttribute oeapiProgrammeRequestParam requestParam) {
        Optional<Programme> existing = service.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There is no Programme with Id: " + id);
        }
        List<Programme> children = existing.get().getChildren();
        return super.getResponse(requestParam.toPageable(), service.toDTOList(children == null ? new ArrayList<>() : children));
    }

    @GetMapping(value = "/{id}/programme-offerings", produces = "application/json")
    public ResponseEntity<?> getOfferings(@PathVariable String id, @ModelAttribute oeapiOfferingRequestParam requestParam) {

        Optional<Programme> existing = service.getById(id);
        if (!existing.isPresent()) {
            throw new oeapiException(HttpStatus.NOT_FOUND, "There are no programme-offerings for Id: " + id);
        }
        List<ProgrammeOffering> programmeOfferings = offeringService.getByProgrammeId(id);

        // If there is no ProgrammeOffering and Autocreate is on, return a basic autogen offering
        if (programmeOfferings.isEmpty()) {
            List<Offering> offerings = new ArrayList<>();
            if (defaultAutoCreateOfferingIfNotExists) {
                offerings.add(offeringService.autoGenerateBasicItem(id));
            }
            return super.getResponse(requestParam, offerings);
        }
        oeapiDTOMapper<ProgrammeOffering, ProgrammeOfferingDTO> mapper =
                new oeapiDTOMapper<ProgrammeOffering, ProgrammeOfferingDTO>(ProgrammeOffering.class,
                        ProgrammeOfferingDTO.class,
                        enumService,
                        Arrays.asList());

        List<ProgrammeOfferingDTO> dtos = mapper.toDTOList(programmeOfferings);
        Pageable pageable = requestParam.toPageable();
        oeapiResponse<ProgrammeOfferingDTO> response = new oeapiResponse<ProgrammeOfferingDTO>(dtos, pageable);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(produces = "application/json")
    @Override
    public ResponseEntity<?> createFromDTO(@Valid @RequestBody ProgrammeDTO dto) {
        return super.createDTO(dto, service);
    }

    @PutMapping(value = "/{programmeId}", produces = "application/json")
    @Override
    public ResponseEntity<?> updateFromDTO(@PathVariable String programmeId, @Valid @RequestBody ProgrammeDTO dto) {
        dto.setProgrammeId(programmeId);
        return super.updateDTO(dto, service);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProgramme(@PathVariable String id) {

        Optional<Programme> existing = service.getById(id);

        if (existing.isPresent()) {
            service.delete(id);
            return ResponseEntity.ok().build();
        } else {
           throw new oeapiException(HttpStatus.NOT_FOUND, "Error delete programme with Id: " + id);
        }
    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByProgrammesJSON(@RequestBody List<ProgrammeDTO> items) {
        return super.createByJSON(items, service);
    }
}
