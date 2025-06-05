package tqs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;

import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.doNothing;


import tqs.backend.dto.StationRequest;
import tqs.backend.model.Charger;
import tqs.backend.model.Discount;
import tqs.backend.model.Station;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.StationRepository;
import tqs.backend.service.StationService;

import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;
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
        public ChargerRepository chargerRepository() {
            return mock(ChargerRepository.class);
        }

        @Bean
        public DiscountRepository discountRepository() {
            return mock(DiscountRepository.class);
        }

        @Bean
        public StationService stationService(StationRepository stationRepository, ChargerRepository chargerRepository, DiscountRepository discountRepository) {
            return new StationService(stationRepository, chargerRepository, discountRepository);
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
                            .requestMatchers("/api/clients/**", "/api/stations/**", "/api/stations/search" , "/api/stations/*/details",  
                            "/api/chargers/**",                             "/api/discounts/**").permitAll()
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

    @Test
    void unexpectedExceptionDuringCreation_shouldReturnBadRequest() throws Exception {
        StationRequest req = new StationRequest("Unexpected", "Rua", "Lisboa", 38.7169, -9.1399);

        StationRepository repository = stationService.getStationRepository();
        when(repository.findByLatitudeAndLongitude(req.getLatitude(), req.getLongitude()))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unexpected error: Database connection error"));
    }

       @Test
        void getStationDetails_shouldReturnStationWithChargers() throws Exception {
        long stationId = 1L;

        Station station = Station.builder()
                .id(stationId)
                .name("Station D")
                .address("Rua Z")
                .city("Lisboa")
                .latitude(38.722)
                .longitude(-9.139)
                .build();

        List<Charger> chargers = List.of(
                Charger.builder()
                        .id(1L)
                        .station(station)
                        .chargerType(ChargerType.DC_FAST)
                        .status(ChargerStatus.AVAILABLE)
                        .pricePerKwh(BigDecimal.valueOf(0.30))
                        .build(),
                Charger.builder()
                        .id(2L)
                        .station(station)
                        .chargerType(ChargerType.AC_STANDARD)
                        .status(ChargerStatus.AVAILABLE)
                        .pricePerKwh(BigDecimal.valueOf(0.20))
                        .build()
        );

        StationRepository stationRepository = stationService.getStationRepository();
        ChargerRepository chargerRepository = stationService.getChargerRepository();

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(chargerRepository.findByStationId(stationId)).thenReturn(chargers);

        mockMvc.perform(get("/api/stations/{id}/details", stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Station D"))
                .andExpect(jsonPath("$.address").value("Rua Z"))
                .andExpect(jsonPath("$.chargers").isArray())
                .andExpect(jsonPath("$.chargers.length()").value(2))
                .andExpect(jsonPath("$.chargers[0].chargerType").value("DC_FAST"))
                .andExpect(jsonPath("$.chargers[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.chargers[0].pricePerKwh").value(0.30));
        }

        @Test
        void deleteExistingStation_shouldReturnNoContent() throws Exception {
        long stationId = 1L;

        StationRepository stationRepository = stationService.getStationRepository();

        when(stationRepository.existsById(stationId)).thenReturn(true);
        doNothing().when(stationRepository).deleteById(stationId);

        mockMvc.perform(delete("/api/stations/{id}", stationId))
                .andExpect(status().isNoContent());
        }

        @Test
        void deleteNonExistingStation_shouldReturnNotFound() throws Exception {
        long stationId = 999L;

        StationRepository stationRepository = stationService.getStationRepository();

        when(stationRepository.existsById(stationId)).thenReturn(false);

        mockMvc.perform(delete("/api/stations/{id}", stationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Station not found"));
        }

        @Test
        @DisplayName("GET /api/stations/search - Should include discount tag when active")
        void searchStations_withActiveDiscount_shouldIncludeDiscountTag() throws Exception {
                // Simula estação real
                Station station = Station.builder()
                        .id(1L)
                        .name("Station Discount")
                        .latitude(37.019)
                        .longitude(-7.93)
                        .build();

                // Simula desconto real
                Discount discount = Discount.builder()
                        .station(station)
                        .chargerType(ChargerType.AC_STANDARD)
                        .dayOfWeek(1)
                        .startHour(14)
                        .endHour(18)
                        .discountPercent(15.0)
                        .active(true)
                        .build();

                // Mocka os repositórios
                StationRepository stationRepo = stationService.getStationRepository();
                DiscountRepository discountRepo = stationService.getDiscountRepository();

                when(stationRepo.findAll()).thenReturn(List.of(station));
                when(discountRepo.findByActiveTrueAndDayOfWeekAndStartHourLessThanEqualAndEndHourGreaterThanEqualAndChargerType(
                        1, 15, 15, ChargerType.AC_STANDARD)).thenReturn(List.of(discount));

                // Chama o endpoint real
                mockMvc.perform(get("/api/stations/search")
                                .param("dayOfWeek", "1")
                                .param("hour", "15")
                                .param("chargerType", "AC_STANDARD")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value("Station Discount"))
                        .andExpect(jsonPath("$[0].discountTag").value("15% off"));
                }



}

