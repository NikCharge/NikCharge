package tqs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.dto.ReservationRequest;
import tqs.backend.model.Charger;
import tqs.backend.model.Client;
import tqs.backend.model.Reservation;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ReservationStatus;
import tqs.backend.service.ReservationService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import tqs.backend.dto.ReservationResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import({ReservationControllerTest.MockConfig.class, ReservationControllerTest.SecurityConfig.class})
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    private Client client;
    private Charger charger;
    private Reservation reservation;
    private ReservationRequest request;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now();
        endTime = startTime.plusHours(2);

        client = Client.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        charger = Charger.builder()
                .id(1L)
                .status(ChargerStatus.AVAILABLE)
                .build();

        reservation = Reservation.builder()
                .id(1L)
                .user(client)
                .charger(charger)
                .startTime(startTime)
                .estimatedEndTime(endTime)
                .batteryLevelStart(20.0)
                .estimatedKwh(30.0)
                .estimatedCost(new BigDecimal("15.00"))
                .status(ReservationStatus.ACTIVE)
                .build();

        request = new ReservationRequest();
        request.setClientId(1L);
        request.setChargerId(1L);
        request.setStartTime(startTime);
        request.setEstimatedEndTime(endTime);
        request.setBatteryLevelStart(20.0);
        request.setEstimatedKwh(30.0);
        request.setEstimatedCost(new BigDecimal("15.00"));

        // Reset mocks before each test
        reset(reservationService);
    }

    @Test
    void whenGetReservationsByClientId_thenReturnListOfReservationResponses() throws Exception {
        // Create sample DTOs
        ReservationResponse.StationDto stationDto = new ReservationResponse.StationDto(
                1L,
                "Test Station",
                "123 Test St",
                "Test City"
        );

        ReservationResponse.ChargerDto chargerDto = new ReservationResponse.ChargerDto(
                101L,
                "DC_FAST",
                stationDto
        );

        LocalDateTime localStartTime = LocalDateTime.now();
        LocalDateTime localEndTime = localStartTime.plusHours(1);
        BigDecimal estimatedCost = new BigDecimal("10.50");

        ReservationResponse reservationResponse = new ReservationResponse(
                201L,
                chargerDto,
                localStartTime,
                localEndTime,
                80.0,
                50.0,
                estimatedCost,
                ReservationStatus.ACTIVE,
                false
        );

        List<ReservationResponse> reservationResponses = Arrays.asList(reservationResponse);

        when(reservationService.getReservationsByClientId(1L)).thenReturn(reservationResponses);

        mockMvc.perform(get("/api/reservations/client/{clientId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(201)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$[0].charger.id", is(101)))
                .andExpect(jsonPath("$[0].charger.chargerType", is("DC_FAST")))
                .andExpect(jsonPath("$[0].charger.station.id", is(1)))
                .andExpect(jsonPath("$[0].charger.station.name", is("Test Station")))
                .andExpect(jsonPath("$[0].charger.station.address", is("123 Test St")))
                .andExpect(jsonPath("$[0].charger.station.city", is("Test City")));

        verify(reservationService, times(1)).getReservationsByClientId(1L);
    }

    @Test
    void whenGetAllReservations_thenReturnReservationsList() throws Exception {
        List<Reservation> reservations = Arrays.asList(reservation);
        when(reservationService.getAllReservations()).thenReturn(reservations);

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$[0].batteryLevelStart", is(20.0)))
                .andExpect(jsonPath("$[0].estimatedKwh", is(30.0)));

        verify(reservationService, times(1)).getAllReservations();
    }

    @Test
    void whenCreateReservation_thenReturnCreatedReservation() throws Exception {
        when(reservationService.createReservation(any(ReservationRequest.class))).thenReturn(reservation);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.batteryLevelStart", is(20.0)))
                .andExpect(jsonPath("$.estimatedKwh", is(30.0)));

        verify(reservationService, times(1)).createReservation(any(ReservationRequest.class));
    }

    @Test
    void whenCreateReservationWithNonExistentClient_thenReturnBadRequest() throws Exception {
        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenThrow(new RuntimeException("Client not found"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Client not found")));

        verify(reservationService, times(1)).createReservation(any(ReservationRequest.class));
    }

    @Test
    void whenCreateReservationWithMaintenanceCharger_thenReturnBadRequest() throws Exception {
        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenThrow(new RuntimeException("This charger is currently under maintenance and cannot be reserved."));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("This charger is currently under maintenance and cannot be reserved.")));

        verify(reservationService, times(1)).createReservation(any(ReservationRequest.class));
    }

    @Test
    void whenCreateReservationWithOverlappingTime_thenReturnBadRequest() throws Exception {
        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenThrow(new RuntimeException("Charger is already reserved for the requested time."));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Charger is already reserved for the requested time.")));

        verify(reservationService, times(1)).createReservation(any(ReservationRequest.class));
    }

    @Test
    void whenGetReservationsByClientIdThrowsException_thenReturnBadRequest() throws Exception {
        when(reservationService.getReservationsByClientId(1L))
                .thenThrow(new RuntimeException("Client not found"));

        mockMvc.perform(get("/api/reservations/client/{clientId}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Client not found")));

        verify(reservationService, times(1)).getReservationsByClientId(1L);
    }

    @Test
    @DisplayName("DELETE /api/reservations/{id} - Successfully cancel an active reservation")
    void whenCancelActiveReservation_thenReturnOk() throws Exception {
        when(reservationService.cancelReservation(1L)).thenReturn(reservation);

        mockMvc.perform(delete("/api/reservations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(reservationService, times(1)).cancelReservation(1L);
    }

    @Test
    @DisplayName("DELETE /api/reservations/{id} - Attempt to cancel non-existent reservation")
    void whenCancelNonExistentReservation_thenReturnNotFound() throws Exception {
        when(reservationService.cancelReservation(999L))
                .thenThrow(new RuntimeException("Reservation not found"));

        mockMvc.perform(delete("/api/reservations/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Reservation not found")));

        verify(reservationService, times(1)).cancelReservation(999L);
    }

    @Test
    @DisplayName("DELETE /api/reservations/{id} - Attempt to cancel already cancelled reservation")
    void whenCancelCompletedReservation_thenReturnBadRequest() throws Exception {
        when(reservationService.cancelReservation(1L))
                .thenThrow(new RuntimeException("Invalid reservation status: only active reservations can be cancelled"));

        mockMvc.perform(delete("/api/reservations/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid reservation status: only active reservations can be cancelled")));

        verify(reservationService, times(1)).cancelReservation(1L);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ReservationService reservationService() {
            return mock(ReservationService.class);
        }
    }

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/reservations/**").permitAll()
                            .anyRequest().authenticated());
            return http.build();
        }
    }
} 