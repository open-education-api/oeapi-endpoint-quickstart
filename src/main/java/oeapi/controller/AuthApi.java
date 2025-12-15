package oeapi.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import oeapi.JwtTokenService;

import oeapi.model.CustomUserDetails;
import oeapi.model.Role;
import oeapi.model.User;
import oeapi.payload.AuthRequest;
import oeapi.payload.ChangePasswordDTO;
import oeapi.repository.RoleRepository;
import oeapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class AuthApi {

    @Autowired
    AuthenticationManager authManager;
    @Autowired
    JwtTokenService jwtUtil;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            String token = jwtUtil.generateAccessToken(userDetails);

            return ResponseEntity.ok(Collections.singletonMap("token", token));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("auth/signup")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest signUpDto) {

        // checking for username exists in a database
        // checking for email exists in a database
        if (userRepository.existsUserByEmail(signUpDto.getEmail())) {
            return new ResponseEntity<>("Email already exists!", HttpStatus.BAD_REQUEST);
        }

        // creating user object
        User user = new User();
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        List<Role> roleList = new ArrayList();

        for (Role r : signUpDto.getRoles()) {
            Role role = roleRepository.findByName(r.getName()).get();
            roleList.add(role);
        }
        user.setRoles(roleList);

        userRepository.save(user);

        return new ResponseEntity<>("User is registered successfully!", HttpStatus.OK);

    }

    @PostMapping("/auth/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO dto, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated successfully");
    }

    @GetMapping("/auth/validate")
    public ResponseEntity<Void> validateToken() {
        // If we get here, Spring Security has already validated the JWT
        return ResponseEntity.ok().build();
    }

    @Value("${ooapi.security.enabled}")
    private boolean ENDPOINT_SECURITY_STATUS_ENABLED;

    @GetMapping("/auth/secStatus")
    public ResponseEntity<?> secStatus() {

      String status = "Disabled by conf. (Updates are allowed without login. Check that's what you want. See doc.) ";
      if (ENDPOINT_SECURITY_STATUS_ENABLED) {
            status = "Enabled by conf, you must login before updates";
        }
      return ResponseEntity.ok(status);
    }
}
