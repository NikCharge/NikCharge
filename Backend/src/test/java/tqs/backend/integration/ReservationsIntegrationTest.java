package tqs.backend.integration;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tqs.backend.model.Charger;
import tqs.backend.model.Client;
import tqs.backend.model.Reservation;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.model.enums.ReservationStatus;
import tqs.backend.model.enums.UserRole;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.ClientRepository;
import tqs.backend.repository.ReservationRepository;
import tqs.backend.repository.StationRepository;
import tqs.backend.service.StripeClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockBean
    private StripeClient stripeClient;

    private Client testClient;
    private Charger testCharger;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setup() throws StripeException {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Mock Stripe client responses
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("cs_test_123");
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", "1");
        when(mockSession.getMetadata()).thenReturn(metadata);
        when(stripeClient.createCheckoutSession(any())).thenReturn(mockSession);
        when(stripeClient.retrieveCheckoutSession(any())).thenReturn(mockSession);

        // Clean up repositories
        reservationRepository.deleteAll();
        clientRepository.deleteAll();
        chargerRepository.deleteAll();
        stationRepository.deleteAll();

        // Create test client with all required fields
        testClient = Client.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .batteryCapacityKwh(70.0)
                .fullRangeKm(350.0)
                .role(UserRole.CLIENT)
                .build();
        testClient = clientRepository.save(testClient);

        // Create test station and charger
        Station station = Station.builder()
                .name("Test Station")
                .address("Test Address")
                .city("Test City")
                .latitude(40.633)
                .longitude(-8.660)
                .build();
        station = stationRepository.save(station);

        testCharger = Charger.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.25))
                .build();
        testCharger = chargerRepository.save(testCharger);

        // Set up test times
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    @DisplayName("Create reservation and verify client relationship")
    void createReservation_VerifiesClientRelationship() {
        // Create reservation
        var reservation = Map.of(
                "clientId", testClient.getId(),
                "chargerId", testCharger.getId(),
                "startTime", startTime.toString(),
                "estimatedEndTime", endTime.toString(),
                "batteryLevelStart", 20.0,
                "estimatedKwh", 30.0,
                "estimatedCost", 7.50
        );

        Integer reservationId = given()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Verify reservation was created and linked to client
        Reservation savedReservation = reservationRepository.findById(reservationId.longValue()).orElseThrow();
        assertThat(savedReservation.getUser().getId()).isEqualTo(testClient.getId());
        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);

        // Verify client has the reservation by checking the reservation's user
        assertThat(savedReservation.getUser().getId()).isEqualTo(testClient.getId());
    }

    @Test
    @DisplayName("Create multiple reservations and verify charger availability")
    void createMultipleReservations_VerifiesChargerAvailability() {
        // Create first reservation
        var firstReservation = Map.of(
                "clientId", testClient.getId(),
                "chargerId", testCharger.getId(),
                "startTime", startTime.toString(),
                "estimatedEndTime", endTime.toString(),
                "batteryLevelStart", 20.0,
                "estimatedKwh", 30.0,
                "estimatedCost", 7.50
        );

        given()
                .contentType(ContentType.JSON)
                .body(firstReservation)
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(201);

        // Try to create overlapping reservation
        var overlappingReservation = Map.of(
                "clientId", testClient.getId(),
                "chargerId", testCharger.getId(),
                "startTime", startTime.plusMinutes(30).toString(),
                "estimatedEndTime", endTime.plusMinutes(30).toString(),
                "batteryLevelStart", 20.0,
                "estimatedKwh", 30.0,
                "estimatedCost", 7.50
        );

        given()
                .contentType(ContentType.JSON)
                .body(overlappingReservation)
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(400)
                .body("error", equalTo("Charger is already reserved for the requested time."));

        // Verify only one reservation exists
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Create reservation and verify charger status")
    void createReservation_VerifiesChargerStatus() {
        // Set charger to maintenance
        testCharger.setStatus(ChargerStatus.UNDER_MAINTENANCE);
        chargerRepository.save(testCharger);

        var reservation = Map.of(
                "clientId", testClient.getId(),
                "chargerId", testCharger.getId(),
                "startTime", startTime.toString(),
                "estimatedEndTime", endTime.toString(),
                "batteryLevelStart", 20.0,
                "estimatedKwh", 30.0,
                "estimatedCost", 7.50
        );

        given()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(400)
                .body("error", equalTo("This charger is currently under maintenance and cannot be reserved."));

        // Verify no reservation was created
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Create reservation and verify station relationship")
    void createReservation_VerifiesStationRelationship() {
        var reservation = Map.of(
                "clientId", testClient.getId(),
                "chargerId", testCharger.getId(),
                "startTime", startTime.toString(),
                "estimatedEndTime", endTime.toString(),
                "batteryLevelStart", 20.0,
                "estimatedKwh", 30.0,
                "estimatedCost", 7.50
        );

        Integer reservationId = given()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Verify reservation is linked to correct charger and station
        Reservation savedReservation = reservationRepository.findById(reservationId.longValue()).orElseThrow();
        assertThat(savedReservation.getCharger().getId()).isEqualTo(testCharger.getId());
        assertThat(savedReservation.getCharger().getStation().getId()).isEqualTo(testCharger.getStation().getId());
    }
} 