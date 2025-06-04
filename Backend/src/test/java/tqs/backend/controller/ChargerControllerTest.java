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
import tqs.backend.model.Charger;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;
import tqs.backend.service.ChargerService;
import tqs.backend.dto.ChargerCreationRequest;
import tqs.backend.dto.ChargerDTO;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChargerController.class)
@Import({ChargerControllerTest.MockConfig.class, ChargerControllerTest.SecurityConfig.class})
@ActiveProfiles("test")
class ChargerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChargerService chargerService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ChargerRepository chargerRepository() {
            return mock(ChargerRepository.class);
        }

        @Bean
        public StationRepository stationRepository() {
            return mock(StationRepository.class);
        }

        @Bean
        public ChargerService chargerService() {
            return mock(ChargerService.class);
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
                            .requestMatchers("/api/chargers/**").permitAll()
                            .anyRequest().authenticated());
            return http.build();
        }
    }

    @Test
    void addCharger_ValidRequest_ReturnsOk() throws Exception {
        ChargerCreationRequest request = new ChargerCreationRequest();
        request.setStationId(1L);
        request.setChargerType(ChargerType.AC_STANDARD);
        request.setStatus(ChargerStatus.AVAILABLE);
        request.setPricePerKwh(BigDecimal.valueOf(0.30));

        Charger savedCharger = Charger.builder()
                .id(5L)
                .chargerType(request.getChargerType())
                .status(request.getStatus())
                .pricePerKwh(request.getPricePerKwh())
                .build();

        when(chargerService.addCharger(eq(request.getStationId()), any(Charger.class)))
                .thenReturn(savedCharger);

        mockMvc.perform(post("/api/chargers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCharger.getId()))
                .andExpect(jsonPath("$.chargerType").value("AC_STANDARD"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.pricePerKwh").value(0.30));
    }

    @Test
    void addCharger_InvalidStationId_ReturnsBadRequest() throws Exception {
        ChargerCreationRequest request = new ChargerCreationRequest();
        request.setStationId(999L);
        request.setChargerType(ChargerType.DC_FAST);
        request.setStatus(ChargerStatus.AVAILABLE);
        request.setPricePerKwh(BigDecimal.valueOf(0.50));

        when(chargerService.addCharger(eq(request.getStationId()), any(Charger.class)))
                .thenThrow(new IllegalArgumentException("Station not found"));

        mockMvc.perform(post("/api/chargers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Station not found"));
    }

    @Test
    void getAllChargers_ReturnsList() throws Exception {
        List<Charger> chargers = List.of(
                Charger.builder().id(1L).chargerType(ChargerType.AC_STANDARD).status(ChargerStatus.AVAILABLE).pricePerKwh(BigDecimal.valueOf(0.20)).build(),
                Charger.builder().id(2L).chargerType(ChargerType.DC_FAST).status(ChargerStatus.AVAILABLE).pricePerKwh(BigDecimal.valueOf(0.40)).build()
        );

        when(chargerService.getAllChargers()).thenReturn(chargers);

        mockMvc.perform(get("/api/chargers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(chargers.size()))
                .andExpect(jsonPath("$[0].chargerType").value("AC_STANDARD"))
                .andExpect(jsonPath("$[1].chargerType").value("DC_FAST"));
    }

    @Test
    void getChargersByStation_ReturnsList() throws Exception {
        Long stationId = 1L;
        List<Charger> chargers = List.of(
                Charger.builder().id(3L).chargerType(ChargerType.AC_STANDARD).status(ChargerStatus.AVAILABLE).pricePerKwh(BigDecimal.valueOf(0.22)).build()
        );

        when(chargerService.getChargersForStation(stationId)).thenReturn(chargers);

        mockMvc.perform(get("/api/chargers/station/{stationId}", stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(chargers.size()))
                .andExpect(jsonPath("$[0].chargerType").value("AC_STANDARD"));
    }

    @Test
    void deleteCharger_ExistingId_ReturnsNoContent() throws Exception {
        Long chargerId = 10L;

        doNothing().when(chargerService).deleteCharger(chargerId);

        mockMvc.perform(delete("/api/chargers/{id}", chargerId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCharger_NonExistingId_ReturnsNotFound() throws Exception {
        Long chargerId = 999L;

        doThrow(new IllegalArgumentException("Charger not found")).when(chargerService).deleteCharger(chargerId);

        mockMvc.perform(delete("/api/chargers/{id}", chargerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Charger not found"));
    }

    @Test
    void countAvailableChargersTotal_ReturnsCount() throws Exception {
        when(chargerService.countByStatus(ChargerStatus.AVAILABLE)).thenReturn(5L);

        mockMvc.perform(get("/api/chargers/count/available/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void countAvailableChargersByStation_ReturnsCount() throws Exception {
        Long stationId = 1L;
        when(chargerService.countByStationAndStatus(stationId, ChargerStatus.AVAILABLE)).thenReturn(3L);

        mockMvc.perform(get("/api/chargers/count/available/station/{stationId}", stationId))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void countInUseChargersTotal_ReturnsCount() throws Exception {
        when(chargerService.countByStatus(ChargerStatus.IN_USE)).thenReturn(2L);

        mockMvc.perform(get("/api/chargers/count/in_use/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void countInUseChargersByStation_ReturnsCount() throws Exception {
        Long stationId = 1L;
        when(chargerService.countByStationAndStatus(stationId, ChargerStatus.IN_USE)).thenReturn(1L);

        mockMvc.perform(get("/api/chargers/count/in_use/station/{stationId}", stationId))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void getAvailableChargers_ReturnsList() throws Exception {
        List<ChargerDTO> availableChargers = List.of(
            ChargerDTO.builder()
                .id(1L)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.20))
                .stationId(101L)
                .stationName("Test Station 1")
                .stationCity("Test City 1")
                .build(),
            ChargerDTO.builder()
                .id(2L)
                .chargerType(ChargerType.DC_FAST)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.40))
                .stationId(102L)
                .stationName("Test Station 2")
                .stationCity("Test City 2")
                .build()
        );

        when(chargerService.getChargersByStatus(ChargerStatus.AVAILABLE))
            .thenReturn(availableChargers);

        mockMvc.perform(get("/api/chargers/available"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(availableChargers.size()))
            .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
            .andExpect(jsonPath("$[1].status").value("AVAILABLE"))
            .andExpect(jsonPath("$[0].chargerType").value("AC_STANDARD"))
            .andExpect(jsonPath("$[1].chargerType").value("DC_FAST"));
    }
}
