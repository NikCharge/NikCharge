package tqs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tqs.backend.service.ClientService;
import tqs.backend.dto.SignUpRequest;
import tqs.backend.model.Client;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import tqs.backend.model.enums.UserRole;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import jakarta.validation.Validator;
import tqs.backend.repository.ClientRepository;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/clients/signup", "/api/clients/login").permitAll()
                        .anyRequest().authenticated());
            return http.build();
        }
    }

    @Test
    void validSignUp_shouldReturnOk() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setBatteryCapacityKwh(50.0);
        request.setFullRangeKm(300.0);

        Client client = new Client();
        client.setId(1L);
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPasswordHash("hashed");
        client.setBatteryCapacityKwh(request.getBatteryCapacityKwh());
        client.setFullRangeKm(request.getFullRangeKm());
        client.setRole(UserRole.CLIENT);

        when(clientService.signUp(any(SignUpRequest.class))).thenReturn(client);

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void signUpWithInvalidEmail_shouldReturnBadRequest() throws Exception {
        SignUpRequest req = new SignUpRequest();
        req.setName("Test");
        req.setEmail("invalid-email");
        req.setPassword("password123");
        req.setBatteryCapacityKwh(45.0);
        req.setFullRangeKm(300.0);

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
        SignUpRequest request = new SignUpRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setBatteryCapacityKwh(50.0);
        request.setFullRangeKm(300.0);

        when(clientService.signUp(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void signUpWithInvalidBatteryCapacity_shouldReturnBadRequest() throws Exception {
        SignUpRequest req = new SignUpRequest();
        req.setName("Test");
        req.setEmail("test@mail.com");
        req.setPassword("password123");
        req.setBatteryCapacityKwh(-1.0);
        req.setFullRangeKm(300.0);

        mockMvc.perform(post("/api/clients/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.batteryCapacityKwh").exists());
    }

    @Test
    void validLogin_shouldReturnOk() throws Exception {
        // Setup
        String email = "login@example.com";
        String rawPassword = "password123";

        Client client = new Client();
        client.setName("LoginUser");
        client.setEmail(email);
        client.setPasswordHash(new BCryptPasswordEncoder().encode(rawPassword));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", rawPassword
        );

        mockMvc.perform(post("/api/clients/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("LoginUser"));
    }

    @Test
    void loginWithWrongPassword_shouldReturnForbidden() throws Exception {
        String email = "login@example.com";

        Client client = new Client();
        client.setEmail(email);
        client.setPasswordHash(new BCryptPasswordEncoder().encode("correctPassword"));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", "wrongPassword"
        );

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
                "password", "somepassword"
        );

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
                .andExpect(jsonPath("$.error.password").value("Password is required"));
    }

}
