package tqs.backend.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tqs.backend.model.Charger;
import tqs.backend.model.Client;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.ClientRepository;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.ReservationRepository;
import tqs.backend.repository.StationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationsApiTest {

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

    @Autowired
    private DiscountRepository discountRepository;


    private Client testClient;
    private Charger testCharger;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

                // Ordem correta para evitar erros de FK
        reservationRepository.deleteAll();
        chargerRepository.deleteAll();

        discountRepository.deleteAll(); 

        stationRepository.deleteAll();
        clientRepository.deleteAll();


        // Create test client
        testClient = Client.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .batteryCapacityKwh(70.0)
                .fullRangeKm(350.0)
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
    @DisplayName("GET /api/reservations - Get all reservations")
    void getAllReservations_ReturnsList() {
        // Create a test reservation first
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
                .statusCode(201);

        // Now get all reservations
        given()
                .when()
                .get("/api/reservations")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("size()", equalTo(1))
                .body("[0].status", equalTo("ACTIVE"))
                .body("[0].batteryLevelStart", equalTo(20.0f))
                .body("[0].estimatedKwh", equalTo(30.0f));
    }

    @Test
    @DisplayName("POST /api/reservations - Create valid reservation")
    void postValidReservation_ReturnsCreated() {
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
                .statusCode(201)
                .body("status", equalTo("ACTIVE"))
                .body("batteryLevelStart", equalTo(20.0f))
                .body("estimatedKwh", equalTo(30.0f));
    }

    @Test
    @DisplayName("POST /api/reservations - Create reservation with non-existent client")
    void postReservationWithNonExistentClient_ReturnsBadRequest() {
        var reservation = Map.of(
                "clientId", 999L,
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
                .body("error", equalTo("Client not found"));
    }

    @Test
    @DisplayName("POST /api/reservations - Create reservation with non-existent charger")
    void postReservationWithNonExistentCharger_ReturnsBadRequest() {
        var reservation = Map.of(
                "clientId", testClient.getId(),
                "chargerId", 999L,
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
                .body("error", equalTo("Charger not found"));
    }

    @Test
    @DisplayName("POST /api/reservations - Create reservation with maintenance charger")
    void postReservationWithMaintenanceCharger_ReturnsBadRequest() {
        // Update charger status to maintenance
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
    }

    @Test
    @DisplayName("POST /api/reservations - Create reservation with overlapping time")
    void postReservationWithOverlappingTime_ReturnsBadRequest() {
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
    }

    @Test
    @DisplayName("GET /api/reservations/client/{clientId} - Get reservations by client ID")
    void getReservationsByClientId_ReturnsListOfReservationResponses() {
        // Create a test reservation first
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
                .statusCode(201);

        // Now get reservations by client ID
        given()
                .when()
                .get("/api/reservations/client/{clientId}", testClient.getId())
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("size()", equalTo(1))
                .body("[0].status", equalTo("ACTIVE"))
                .body("[0].batteryLevelStart", equalTo(20.0f))
                .body("[0].estimatedKwh", equalTo(30.0f))
                .body("[0].charger.id", equalTo(testCharger.getId().intValue()))
                .body("[0].charger.chargerType", equalTo(testCharger.getChargerType().toString()))
                .body("[0].charger.station.id", equalTo(testCharger.getStation().getId().intValue()))
                .body("[0].charger.station.name", equalTo(testCharger.getStation().getName()))
                .body("[0].charger.station.address", equalTo(testCharger.getStation().getAddress()))
                .body("[0].charger.station.city", equalTo(testCharger.getStation().getCity()));
    }
} 