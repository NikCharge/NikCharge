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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tqs.backend.dto.StationRequest;
import tqs.backend.model.Station;
import tqs.backend.repository.StationRepository;
import tqs.backend.service.StationService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StationController.class)
@Import({ StationControllerTest.MockConfig.class, StationControllerTest.SecurityConfig.class })
@ActiveProfiles("test")
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StationService stationService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public StationRepository stationRepository() {
            return mock(StationRepository.class);
        }

        @Bean
        public StationService stationService(StationRepository stationRepository) {
            return new StationService(stationRepository);
        }

        @Bean
        public Validator validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/clients/**", "/api/stations/**").permitAll()
                            .anyRequest().authenticated());
            return http.build();
        }
    }

    // -------------------- TESTS --------------------

    @Test
    void validStationCreation_shouldReturnOk() throws Exception {
        StationRequest req = new StationRequest("Station A", "Rua X", "Aveiro", 40.633, -8.660);
        Station saved = Station.builder()
                .id(1L)
                .name(req.getName())
                .address(req.getAddress())
                .city(req.getCity())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .build();

        StationRepository repository = stationService.getStationRepository();
        when(repository.findByLatitudeAndLongitude(req.getLatitude(), req.getLongitude()))
                .thenReturn(Optional.empty());
        when(repository.save(any(Station.class))).thenReturn(saved);

        mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Station A"))
                .andExpect(jsonPath("$.city").value("Aveiro"));
    }

    @Test
    void duplicateCoordinates_shouldReturnConflict() throws Exception {
        StationRequest req = new StationRequest("Station A", "Rua X", "Aveiro", 40.633, -8.660);
        Station existing = Station.builder()
                .id(1L)
                .name("Existing Station")
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .build();

        StationRepository repository = stationService.getStationRepository();
        when(repository.findByLatitudeAndLongitude(req.getLatitude(), req.getLongitude()))
                .thenReturn(Optional.of(existing));

        mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Station already exists at this location"));
    }

    @Test
    void missingFields_shouldReturnBadRequest() throws Exception {
        StationRequest req = new StationRequest(); // Missing all required fields

        mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.name").exists())
                .andExpect(jsonPath("$.error.address").exists())
                .andExpect(jsonPath("$.error.city").exists())
                .andExpect(jsonPath("$.error.latitude").exists())
                .andExpect(jsonPath("$.error.longitude").exists());
    }

    @Test
    void getAllStations_shouldReturnList() throws Exception {
        List<Station> stations = List.of(
                Station.builder().id(1L).name("S1").latitude(40.1).longitude(-8.1).build(),
                Station.builder().id(2L).name("S2").latitude(41.2).longitude(-8.2).build());

        StationRepository repository = stationService.getStationRepository();
        when(repository.findAll()).thenReturn(stations);

        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getStationById_shouldReturnStation() throws Exception {
        Station station = Station.builder()
                .id(1L)
                .name("Station 1")
                .address("Rua Y")
                .city("Porto")
                .latitude(40.5)
                .longitude(-8.5)
                .build();

        StationRepository repository = stationService.getStationRepository();
        when(repository.findById(1L)).thenReturn(Optional.of(station));

        mockMvc.perform(get("/api/stations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Station 1"));
    }
}
