package tqs.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tqs.backend.model.Client;
import tqs.backend.repository.ClientRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email and password required"));
        }
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isPresent() && passwordEncoder.matches(password, clientOpt.get().getPasswordHash())) {
            // Return a dummy token for now
            return ResponseEntity.ok(Map.of("token", "dummy-token", "email", email));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid credentials"));
        }
    }
}