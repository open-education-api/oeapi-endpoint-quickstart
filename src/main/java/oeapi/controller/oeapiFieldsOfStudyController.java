package oeapi.controller;

import oeapi.controller.requestparameters.oeapiFieldsOfStudyRequestParam;
import java.util.Map;
import oeapi.model.oeapiFieldsOfStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oeapi.repository.oeapiFieldsOfStudyRepository;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/fieldsofstudy")
public class oeapiFieldsOfStudyController {

    @Autowired
    private oeapiFieldsOfStudyRepository repo;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiFieldsOfStudyRequestParam requestParam) {

        Page<oeapiFieldsOfStudy> pages;
        Map.Entry<String, String> filter = requestParam.getFilter();
        if (filter == null) {
            pages = repo.findAll(requestParam.toPageable());
        } else {
            pages = repo.findByLevel(Long.parseLong(filter.getValue()), requestParam.toPageable());
        }
        oeapiResponse response = new oeapiResponse(pages);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}
