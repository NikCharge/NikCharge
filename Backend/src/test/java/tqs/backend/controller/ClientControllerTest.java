package tqs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tqs.backend.dto.ClientResponse;
import tqs.backend.dto.SignUpRequest;
import tqs.backend.model.Client;
import tqs.backend.model.enums.UserRole;
import tqs.backend.repository.ClientRepository;
import tqs.backend.service.ClientService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import({ ClientControllerTest.MockConfig.class, ClientControllerTest.SecurityConfig.class })
@ActiveProfiles("test")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ClientService clientService() {
            return mock(ClientService.class);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public Validator validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        public ClientRepository clientRepository() {
            return mock(ClientRepository.class);
        }
    }

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/clients/**").permitAll()
                            .anyRequest().authenticated());
            return http.build();
        }
    }

    // ---------- SIGN UP TESTS ----------

    @Test
    void validSignUp_shouldReturnOk() throws Exception {
        SignUpRequest request = new SignUpRequest("John Doe", "john@example.com", "password123", 50.0, 300.0);
        Client client = new Client(null, "John Doe", "john@example.com", "hashed", UserRole.CLIENT, 50.0, 300.0, new ArrayList<>());

        when(clientService.signUp(any(SignUpRequest.class))).thenReturn(client);

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void signUpWithInvalidEmail_shouldReturnBadRequest() throws Exception {
        SignUpRequest req = new SignUpRequest("Test", "invalid-email", "password123", 45.0, 300.0);

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.email").exists());
    }

    @Test
    void signUpWithMissingRequiredFields_shouldReturnBadRequest() throws Exception {
        SignUpRequest req = new SignUpRequest();
        req.setName("Test");
        req.setPassword("password123");

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.email").exists());
    }

    @Test
    void signUpWithDuplicateEmail_shouldReturnConflict() throws Exception {
        SignUpRequest request = new SignUpRequest("John Doe", "john@example.com", "password123", 50.0, 300.0);

        when(clientService.signUp(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void signUpWithNegativeBatteryCapacity_shouldReturnBadRequest() throws Exception {
        SignUpRequest req = new SignUpRequest("Test", "test@mail.com", "password123", -1.0, 300.0);

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.batteryCapacityKwh").exists());
    }

    @Test
    void signUpWithNegativeFullRange_shouldReturnBadRequest() throws Exception {
        SignUpRequest req = new SignUpRequest("Test", "test@mail.com", "password123", 50.0, -100.0);

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fullRangeKm").exists());
    }

    @Test
    void signUpWithUnexpectedError_shouldReturnBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("Jane Doe", "jane.doe@example.com", "securepassword", 60.0, 400.0);
        String errorMessage = "Some unexpected error occurred";

        when(clientService.signUp(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unexpected error: " + errorMessage));
    }

    // ---------- LOGIN TESTS ----------

    @Test
    void validLogin_shouldReturnOk() throws Exception {
        String email = "login@example.com";
        String rawPassword = "password123";

        Client client = new Client();
        client.setName("LoginUser");
        client.setEmail(email);
        client.setPasswordHash(new BCryptPasswordEncoder().encode(rawPassword));
        client.setBatteryCapacityKwh(45.0);
        client.setFullRangeKm(320.0);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", rawPassword);

        mockMvc.perform(post("/api/clients/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("LoginUser"))
                .andExpect(jsonPath("$.batteryCapacityKwh").value(45.0))
                .andExpect(jsonPath("$.fullRangeKm").value(320.0));
    }

    @Test
    void loginWithWrongPassword_shouldReturnForbidden() throws Exception {
        String email = "login@example.com";

        Client client = new Client();
        client.setEmail(email);
        client.setPasswordHash(new BCryptPasswordEncoder().encode("correctPassword"));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        Map<String, String> loginRequest = Map.of("email", email, "password", "wrongPassword");

        mockMvc.perform(post("/api/clients/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void loginWithNonExistentEmail_shouldReturnForbidden() throws Exception {
        when(clientRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        Map<String, String> loginRequest = Map.of(
                "email", "nope@example.com",
                "password", "somepassword");

        mockMvc.perform(post("/api/clients/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void loginWithMissingFields_shouldReturnBadRequest() throws Exception {
        Map<String, String> loginRequest = Map.of("email", "someone@example.com");

        mockMvc.perform(post("/api/clients/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.password").exists());
    }

    @Test
    void loginWithInvalidEmailFormat_shouldReturnBadRequest() throws Exception {
        Map<String, String> loginRequest = Map.of("email", "invalid@", "password", "pass123");

        mockMvc.perform(post("/api/clients/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.email").exists());
    }

    // ---------- UPDATE CLIENT TESTS ----------

    @Test
    void updateExistingClient_shouldReturnUpdatedClient() throws Exception {
        String existingEmail = "john@example.com";
        Client client = new Client();
        client.setEmail(existingEmail);
        client.setName("Old Name");
        client.setBatteryCapacityKwh(40.0);
        client.setFullRangeKm(250.0);

        when(clientRepository.findByEmail(existingEmail)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientResponse updatedData = new ClientResponse(1L, "email@example.com", "Name", 50.0, 300.0, new ArrayList<>());

        mockMvc.perform(put("/api/clients/{email}", existingEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@example.com"))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.batteryCapacityKwh").value(50.0))
                .andExpect(jsonPath("$.fullRangeKm").value(300.0));
    }

    @Test
    void updateNonExistentClient_shouldReturnNotFound() throws Exception {
        String email = "notfound@example.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        ClientResponse updateData = new ClientResponse(1L, "email@example.com", "Name", 50.0, 300.0, new ArrayList<>());

        mockMvc.perform(put("/api/clients/{email}", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Client not found"));
    }

    // ---------- CHANGE ROLE TESTS ----------

    @Test
    void changeRole_ExistingClientValidRole_shouldReturnOk() throws Exception {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);
        client.setEmail("client@example.com");
        client.setRole(UserRole.CLIENT);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Map<String, String> requestBody = Map.of("newRole", "EMPLOYEE");

        mockMvc.perform(put("/api/clients/changeRole/{id}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated successfully"))
                .andExpect(jsonPath("$.newRole").value("EMPLOYEE"));
    }

    @Test
    void changeRole_NonExistentClient_shouldReturnNotFound() throws Exception {
        Long clientId = 99L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Map<String, String> requestBody = Map.of("newRole", "EMPLOYEE");

        mockMvc.perform(put("/api/clients/changeRole/{id}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Client not found"));
    }

    @Test
    void changeRole_MissingNewRoleField_shouldReturnBadRequest() throws Exception {
        Long clientId = 1L;
        Map<String, String> requestBody = new HashMap<>(); // Missing newRole field

        mockMvc.perform(put("/api/clients/changeRole/{id}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing 'newRole' in request body"));
    }

    @Test
    void changeRole_InvalidRoleValue_shouldReturnBadRequest() throws Exception {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Map<String, String> requestBody = Map.of("newRole", "INVALID_ROLE");

        mockMvc.perform(put("/api/clients/changeRole/{id}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid role: INVALID_ROLE"));
    }

    // ---------- GET CLIENT BY ID TESTS ----------

    @Test
    void getClientById_ExistingClient_shouldReturnClientData() throws Exception {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);
        client.setName("Test User");
        client.setEmail("test@example.com");
        client.setRole(UserRole.CLIENT);
        client.setBatteryCapacityKwh(50.0);
        client.setFullRangeKm(300.0);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.batteryCapacityKwh").value(50.0))
                .andExpect(jsonPath("$.fullRangeKm").value(300.0));
    }

    @Test
    void getClientById_NonExistentClient_shouldReturnNotFound() throws Exception {
        Long clientId = 99L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Client not found"));
    }
}
