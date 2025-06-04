package tqs.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tqs.backend.dto.SignUpRequest;
import tqs.backend.dto.LoginRequest;
import tqs.backend.dto.ClientResponse;
import tqs.backend.model.Client;
import tqs.backend.repository.ClientRepository;
import tqs.backend.service.ClientService;
import tqs.backend.model.enums.UserRole;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private static final String CLIENT_NOT_FOUND_MESSAGE = "Client not found";

    private final ClientService clientService;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(Map.of("error", errors));
        }

        try {
            Client client = clientService.signUp(signUpRequest);
            return ResponseEntity.ok(new ClientResponse(
                client.getEmail(),
                client.getName(),
                client.getBatteryCapacityKwh(),
                client.getFullRangeKm(),
                client.getRole()
            ));
        } catch (RuntimeException e) {
            if ("Email already exists".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(Map.of("error", errors));
        }

        Optional<Client> clientOpt = clientRepository.findByEmail(loginRequest.getEmail());
        if (clientOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), clientOpt.get().getPasswordHash())) {
            Client client = clientOpt.get();
            ClientResponse response = new ClientResponse(
                    client.getEmail(),
                    client.getName(),
                    client.getBatteryCapacityKwh(),
                    client.getFullRangeKm(),
                    client.getRole()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PutMapping("/{email}")
    public ResponseEntity<Object> updateClient(@PathVariable String email, @RequestBody ClientResponse updateData) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", CLIENT_NOT_FOUND_MESSAGE));
        }

        Client client = clientOpt.get();
        client.setName(updateData.getName());
        client.setEmail(updateData.getEmail());
        client.setBatteryCapacityKwh(updateData.getBatteryCapacityKwh());
        client.setFullRangeKm(updateData.getFullRangeKm());
        client.setRole(updateData.getRole());

        clientRepository.save(client);

        return ResponseEntity.ok(new ClientResponse(
                client.getEmail(),
                client.getName(),
                client.getBatteryCapacityKwh(),
                client.getFullRangeKm(),
                client.getRole()
        ));
    }

    @PutMapping("/changeRole/{id}")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRoleStr = body.get("newRole");

        if (newRoleStr == null || newRoleStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'newRole' in request body"));
        }

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(newRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + newRoleStr));
        }

        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", CLIENT_NOT_FOUND_MESSAGE));
        }

        Client client = clientOpt.get();
        client.setRole(newRole);
        clientRepository.save(client);

        return ResponseEntity.ok(Map.of(
                "message", "Role updated successfully",
                "email", client.getEmail(),
                "newRole", client.getRole().name()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", CLIENT_NOT_FOUND_MESSAGE));
        }

        Client client = clientOpt.get();

        Map<String, Object> clientData = new HashMap<>();
        clientData.put("id", client.getId());
        clientData.put("name", client.getName());
        clientData.put("email", client.getEmail());
        clientData.put("role", client.getRole());
        clientData.put("batteryCapacityKwh", client.getBatteryCapacityKwh());
        clientData.put("fullRangeKm", client.getFullRangeKm());

        return ResponseEntity.ok(clientData);
    }


}
