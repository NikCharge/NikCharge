package tqs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.model.Reservation;
import tqs.backend.service.ReservationService;
import tqs.backend.service.StripeClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;

@WebMvcTest(PaymentController.class)
@Import({PaymentControllerTest.SecurityConfig.class})
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private StripeClient stripeClient;

    private Reservation mockReservation;
    private Session mockSession;

    @BeforeEach
    void setUp() throws StripeException {
        mockReservation = mock(Reservation.class);
        when(mockReservation.getId()).thenReturn(1L);
        when(mockReservation.getEstimatedCost()).thenReturn(new BigDecimal("10.00"));
        when(mockReservation.isPaid()).thenReturn(false);
        when(mockReservation.getStatus()).thenReturn(tqs.backend.model.enums.ReservationStatus.ACTIVE);

        mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("cs_test_123");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", "1");
        when(mockSession.getMetadata()).thenReturn(metadata);
        when(mockSession.getPaymentStatus()).thenReturn("paid");
    }

    @Test
    @DisplayName("Create checkout session - Success")
    void whenCreateCheckoutSession_thenReturnSessionId() throws Exception {
        when(reservationService.getReservationById(1L)).thenReturn(mockReservation);
        when(stripeClient.createCheckoutSession(any(SessionCreateParams.class))).thenReturn(mockSession);

        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("reservationId", 1L);

        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is("cs_test_123")));

        verify(reservationService, times(1)).getReservationById(1L);
        verify(stripeClient, times(1)).createCheckoutSession(any(SessionCreateParams.class));
    }

    @Test
    @DisplayName("Create checkout session - Invalid reservation status")
    void whenCreateCheckoutSessionWithInvalidStatus_thenReturnBadRequest() throws Exception {
        when(mockReservation.getStatus()).thenReturn(null);
        when(reservationService.getReservationById(1L)).thenReturn(mockReservation);

        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("reservationId", 1L);

        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Reservation cannot be paid")));

        verify(reservationService, times(1)).getReservationById(1L);
        verify(stripeClient, never()).createCheckoutSession(any());
    }

    @Test
    @DisplayName("Create checkout session - Missing reservationId")
    void whenCreateCheckoutSessionWithoutReservationId_thenReturnBadRequest() throws Exception {
        Map<String, Long> requestBody = new HashMap<>();
        // missing reservationId

        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing reservationId")));

        verify(reservationService, never()).getReservationById(any());
        verify(stripeClient, never()).createCheckoutSession(any());
    }

     @Test
    @DisplayName("Create checkout session - Reservation not found")
    void whenCreateCheckoutSessionForNonexistentReservation_thenReturnNotFound() throws Exception {
        when(reservationService.getReservationById(999L)).thenReturn(null);

        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("reservationId", 999L);

        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Reservation not found")));

        verify(reservationService, times(1)).getReservationById(999L);
        verify(stripeClient, never()).createCheckoutSession(any());
    }

    @Test
    @DisplayName("Create checkout session - Reservation already paid")
    void whenCreateCheckoutSessionForPaidReservation_thenReturnBadRequest() throws Exception {
        when(mockReservation.isPaid()).thenReturn(true);
        when(reservationService.getReservationById(1L)).thenReturn(mockReservation);

        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("reservationId", 1L);

        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Reservation is already paid")));

        verify(reservationService, times(1)).getReservationById(1L);
        verify(stripeClient, never()).createCheckoutSession(any());
    }

    @Test
    @DisplayName("Create checkout session - Reservation with no estimated cost")
    void whenCreateCheckoutSessionForReservationWithoutCost_thenReturnBadRequest() throws Exception {
        when(mockReservation.getEstimatedCost()).thenReturn(null);
        when(reservationService.getReservationById(1L)).thenReturn(mockReservation);

        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("reservationId", 1L);

        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Reservation cannot be paid")));

        verify(reservationService, times(1)).getReservationById(1L);
        verify(stripeClient, never()).createCheckoutSession(any());
    }

    // Test cases for /api/payment/verify-session
    @Test
    @DisplayName("Verify checkout session - Success")
    void whenVerifyCheckoutSession_thenMarkReservationAsPaid() throws Exception {
        String testSessionId = "cs_test_123";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", "1");

        when(stripeClient.retrieveCheckoutSession(testSessionId)).thenReturn(mockSession);
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        when(mockSession.getMetadata()).thenReturn(metadata);
        when(reservationService.getReservationById(1L)).thenReturn(mockReservation);
        when(reservationService.saveReservation(any(Reservation.class))).thenReturn(mockReservation);

        mockMvc.perform(get("/api/payment/verify-session")
                .param("session_id", testSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Payment successfully verified and reservation updated")));

        verify(stripeClient, times(1)).retrieveCheckoutSession(testSessionId);
        verify(mockSession, times(1)).getPaymentStatus();
        verify(mockSession, times(1)).getMetadata();
        verify(reservationService, times(1)).getReservationById(1L);
        verify(reservationService, times(1)).saveReservation(any(Reservation.class));
        verify(mockReservation, times(1)).setPaid(true);
    }

    @Test
    @DisplayName("Verify checkout session - Payment not completed")
    void whenVerifyCheckoutSessionWithUnpaidSession_thenReturnBadRequest() throws Exception {
        String testSessionId = "cs_test_unpaid";

        when(stripeClient.retrieveCheckoutSession(testSessionId)).thenReturn(mockSession);
        when(mockSession.getPaymentStatus()).thenReturn("unpaid");

        mockMvc.perform(get("/api/payment/verify-session")
                .param("session_id", testSessionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Payment not completed")));

        verify(stripeClient, times(1)).retrieveCheckoutSession(testSessionId);
        verify(mockSession, times(1)).getPaymentStatus();
        verify(mockSession, never()).getMetadata();
        verify(reservationService, never()).getReservationById(any());
        verify(reservationService, never()).saveReservation(any());
    }

    @Test
    @DisplayName("Verify checkout session - Missing reservationId metadata")
    void whenVerifyCheckoutSessionWithMissingMetadata_thenReturnBadRequest() throws Exception {
        String testSessionId = "cs_test_missing_metadata";
        Map<String, String> metadata = new HashMap<>(); // Missing reservationId

        when(stripeClient.retrieveCheckoutSession(testSessionId)).thenReturn(mockSession);
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        when(mockSession.getMetadata()).thenReturn(metadata);

        mockMvc.perform(get("/api/payment/verify-session")
                .param("session_id", testSessionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Reservation ID not found in session metadata")));

        verify(stripeClient, times(1)).retrieveCheckoutSession(testSessionId);
        verify(mockSession, times(1)).getPaymentStatus();
        verify(mockSession, times(1)).getMetadata();
        verify(reservationService, never()).getReservationById(any());
        verify(reservationService, never()).saveReservation(any());
    }

    @Test
    @DisplayName("Verify checkout session - Invalid reservationId metadata format")
    void whenVerifyCheckoutSessionWithInvalidMetadataFormat_thenReturnBadRequest() throws Exception {
        String testSessionId = "cs_test_invalid_metadata";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", "abc"); // Invalid format

        when(stripeClient.retrieveCheckoutSession(testSessionId)).thenReturn(mockSession);
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        when(mockSession.getMetadata()).thenReturn(metadata);

        mockMvc.perform(get("/api/payment/verify-session")
                .param("session_id", testSessionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid reservation ID in session metadata")));

        verify(stripeClient, times(1)).retrieveCheckoutSession(testSessionId);
        verify(mockSession, times(1)).getPaymentStatus();
        verify(mockSession, times(1)).getMetadata();
        verify(reservationService, never()).getReservationById(any());
        verify(reservationService, never()).saveReservation(any());
    }

    @Test
    @DisplayName("Verify checkout session - Reservation not found after verification")
    void whenVerifyCheckoutSessionAndReservationNotFound_thenReturnNotFound() throws Exception {
        String testSessionId = "cs_test_not_found";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", "999");

        when(stripeClient.retrieveCheckoutSession(testSessionId)).thenReturn(mockSession);
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        when(mockSession.getMetadata()).thenReturn(metadata);
        when(reservationService.getReservationById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/payment/verify-session")
                .param("session_id", testSessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Reservation not found")));

        verify(stripeClient, times(1)).retrieveCheckoutSession(testSessionId);
        verify(mockSession, times(1)).getPaymentStatus();
        verify(mockSession, times(1)).getMetadata();
        verify(reservationService, times(1)).getReservationById(999L);
        verify(reservationService, never()).saveReservation(any());
    }

    @Test
    @DisplayName("Verify checkout session - Reservation already paid after verification")
    void whenVerifyCheckoutSessionAndReservationAlreadyPaid_thenReturnOk() throws Exception {
        String testSessionId = "cs_test_already_paid";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", "1");

        when(stripeClient.retrieveCheckoutSession(testSessionId)).thenReturn(mockSession);
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        when(mockSession.getMetadata()).thenReturn(metadata);
        when(mockReservation.isPaid()).thenReturn(true);
        when(reservationService.getReservationById(1L)).thenReturn(mockReservation);

        mockMvc.perform(get("/api/payment/verify-session")
                .param("session_id", testSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Reservation already marked as paid")));

        verify(stripeClient, times(1)).retrieveCheckoutSession(testSessionId);
        verify(mockSession, times(1)).getPaymentStatus();
        verify(mockSession, times(1)).getMetadata();
        verify(reservationService, times(1)).getReservationById(1L);
        verify(reservationService, never()).saveReservation(any());
    }

    // TODO: Add more tests for other cases in verify-session (missing metadata, reservation not found, already paid, invalid ID format)

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/payment/**").permitAll()
                    .anyRequest().authenticated());
            return http.build();
        }
    }
} 