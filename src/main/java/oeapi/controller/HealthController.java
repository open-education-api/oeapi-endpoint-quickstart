package oeapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import oeapi.repository.UserRepository;

@RestController("/health")
public class HealthController {
    @Autowired
    UserRepository userRepository;

    public static String MESSAGE_NO_DATABASE = "no database";
    public static String MESSAGE_OK = "up and running";

    @GetMapping
    public ResponseEntity<?> health() {
        try {
            // count users to see if database is accessible
            userRepository.count();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MESSAGE_NO_DATABASE);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(MESSAGE_OK);
    }
}
