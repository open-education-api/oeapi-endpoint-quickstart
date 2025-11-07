package oeapi.controller;

import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.ArrayList;
import java.util.Arrays;

import oeapi.model.EducationSpecification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/news-item")
public class NewsItemController extends oeapiController<EducationSpecification> {

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiRequestParam requestParam) {
        return super.getResponse(requestParam.toPageable(Arrays.asList("newsId", "name")), new ArrayList());

    }
}
