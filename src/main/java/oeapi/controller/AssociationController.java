package oeapi.controller;

import java.util.Optional;

import oeapi.model.Association;
import oeapi.service.AssociationService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/associations")
public class AssociationController {

    @Autowired
    private AssociationService service;

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {

        Optional<Association> a = service.getById(id);
        if (!a.isPresent()) {
            Association aAuto = service.autoGenerateBasicItem(id);
            return ResponseEntity.ok(aAuto);
            //return ResponseEntity.badRequest().body("Error: " + id + " not found");
        } else {
            Association obj = a.get();

            return ResponseEntity.ok(obj);
        }
    }

    @PostMapping("/external/me")
    public ResponseEntity<?> create(@RequestBody Association association) {

        Association a = service.create(association);

        return ResponseEntity.ok(a);

    }

}
