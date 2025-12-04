package oeapi.controller;

import oeapi.model.Metadata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Carlos Alonso <losalo@unavarra.es>
 */
@RestController
@RequestMapping("/")
public class MetadataController {

    private final Metadata metadata;

    public MetadataController(Metadata metadata) {
        this.metadata = metadata;
    }

    @GetMapping(produces = "application/json")
    public Metadata getMetadata() {
        return metadata;
    }

}
