package oeapi.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.controller.requestparameters.oeapiRequestParam;
import oeapi.model.Role;
import oeapi.repository.RoleRepository;

@RestController
@RequestMapping("/admin/roles")
public class RoleController {
    @Autowired
    RoleRepository roleRepository;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiRequestParam requestParam) {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }
}
