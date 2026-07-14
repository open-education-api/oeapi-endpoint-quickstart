package oeapi.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import oeapi.controller.requestparameters.oeapiRequestParam;
import oeapi.model.User;
import oeapi.repository.UserRepository;

@RestController
@RequestMapping("/admin/users")
public class UserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiRequestParam requestParam) {
        List<User> users = userRepository.findAll().stream().map(user -> cleanupUser(user)).toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable Integer id) {
        Optional<User> maybeUser = userRepository.findById(id);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = cleanupUser(maybeUser.get());
        return ResponseEntity.ok(user);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        if (userRepository.existsUserByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(cleanupUser(user));
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> put(@PathVariable Integer id, @Valid @RequestBody User user) {
        Optional<User> maybeUser = userRepository.findById(id);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User original = maybeUser.get();
        original.setEmail(user.getEmail());
        if (user.getPassword() != null) {
            original.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        original.setRoles(user.getRoles());
        userRepository.save(original);

        return ResponseEntity.ok(cleanupUser(original));
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Optional<User> maybeUser = userRepository.findById(id);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = maybeUser.get();
        userRepository.delete(user);

        return ResponseEntity.ok(cleanupUser(user));
    }



    private User cleanupUser(User user) {
        User clean = new User();
        clean.setId(user.getId());
        clean.setEmail(user.getEmail());
        clean.setRoles(user.getRoles());
        return clean;
    }
}
