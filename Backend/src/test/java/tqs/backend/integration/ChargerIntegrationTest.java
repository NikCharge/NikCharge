package tqs.backend.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChargerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        // Clean up before each test
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
    }

    // Helper to create a station in the database for testing
    private Station createStation() {
        // Clean up existing data
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
        stationRepository.flush(); // Ensure cleanup is complete

        Station station = Station.builder()
                .name("Integration Test Station")
                .address("Rua Teste")
                .city("Aveiro")
                // Use larger increments to ensure uniqueness
                .latitude(40.63 + (Math.random() * 10.0))  // Add random offset between 0 and 10
                .longitude(-8.66 + (Math.random() * 10.0)) // Add random offset between 0 and 10
                .build();
        station = stationRepository.save(station);
        stationRepository.flush(); // Ensure station is saved
        return station;
    }

    // ---------- ADD CHARGER ----------

    @Test
    void testAddCharger_ValidRequest_ReturnsOk() {
        Station station = createStation();

        Map<String, Object> payload = Map.of(
                "stationId", station.getId(),
                "chargerType", "AC_STANDARD",
                "status", "AVAILABLE",
                "pricePerKwh", 0.30
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().post("/api/chargers")
                .then().statusCode(200)
                .body("id", notNullValue())
                .body("chargerType", equalTo("AC_STANDARD"))
                .body("status", equalTo("AVAILABLE"))
                .body("pricePerKwh", equalTo(0.30f));
    }

    @Test
    void testAddCharger_InvalidStationId_ReturnsBadRequest() {
        Map<String, Object> payload = Map.of(
                "stationId", 999999L,  // Non-existent station
                "chargerType", "DC_FAST",
                "status", "AVAILABLE",
                "pricePerKwh", 0.50
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().post("/api/chargers")
                .then().statusCode(400)
                .body("error", containsString("Station not found"));
    }

    // ---------- GET ALL CHARGERS ----------

    @Test
    void testGetAllChargers_ReturnsList() {
        Station station = createStation();

        Charger c1 = Charger.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.20))
                .build();

        Charger c2 = Charger.builder()
                .station(station)
                .chargerType(ChargerType.DC_FAST)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.40))
                .build();

        chargerRepository.saveAll(List.of(c1, c2));

        given().when().get("/api/chargers")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .body("[0].chargerType", anyOf(equalTo("AC_STANDARD"), equalTo("DC_FAST")))
                .body("[1].chargerType", anyOf(equalTo("AC_STANDARD"), equalTo("DC_FAST")));
    }

    // ---------- GET CHARGERS BY STATION ----------

    @Test
    void testGetChargersByStation_ReturnsList() {
        Station station = createStation();

        Charger c = Charger.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.22))
                .build();

        chargerRepository.save(c);

        given().when().get("/api/chargers/station/{stationId}", station.getId())
                .then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].chargerType", equalTo("AC_STANDARD"));
    }

    // ---------- MARK CHARGER UNDER MAINTENANCE ----------

    @Test
    void testMarkChargerUnderMaintenance_ExistingCharger_ReturnsOk() {
        Station station = createStation();
        Charger charger = Charger.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.30))
                .build();
        charger = chargerRepository.save(charger);

        Map<String, String> payload = Map.of(
                "status", "UNDER_MAINTENANCE",
                "maintenanceNote", "Routine check"
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().put("/api/chargers/{id}/status", charger.getId())
                .then().statusCode(200)
                .body("id", equalTo(charger.getId().intValue()))
                .body("status", equalTo("UNDER_MAINTENANCE"))
                .body("maintenanceNote", equalTo("Routine check"));

        // Verify the status was actually updated in the database
        Charger updatedCharger = chargerRepository.findById(charger.getId()).orElse(null);
        assertThat(updatedCharger).isNotNull();
        assertThat(updatedCharger.getStatus()).isEqualTo(ChargerStatus.UNDER_MAINTENANCE);
        assertThat(updatedCharger.getMaintenanceNote()).isEqualTo("Routine check");
    }

    @Test
    void testMarkChargerUnderMaintenance_NonExistingCharger_ReturnsNotFound() {
        Long nonExistentChargerId = 999999L;
        Map<String, String> payload = Map.of(
                "status", "UNDER_MAINTENANCE",
                "maintenanceNote", "Faulty charger"
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().put("/api/chargers/{id}/status", nonExistentChargerId)
                .then().statusCode(404)
                .body("error", containsString("Charger not found"));
    }

    // ---------- GET AVAILABLE CHARGERS AFTER MAINTENANCE ----------

    @Test
    void testGetAvailableChargers_ExcludesUnderMaintenance() {
        Station station = createStation();

        Charger availableCharger = Charger.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.25))
                .build();

        Charger maintenanceCharger = Charger.builder()
                .station(station)
                .chargerType(ChargerType.DC_FAST)
                .status(ChargerStatus.UNDER_MAINTENANCE)
                .pricePerKwh(BigDecimal.valueOf(0.50))
                .maintenanceNote("Needs repair")
                .build();

        chargerRepository.saveAll(List.of(availableCharger, maintenanceCharger));

        given().when().get("/api/chargers/available")
                .then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(availableCharger.getId().intValue()))
                .body("[0].status", equalTo("AVAILABLE"));
    }

    // ---------- MARK CHARGER AVAILABLE ----------

    @Test
    void testMarkChargerAvailable_ExistingCharger_ReturnsOk() {
        Station station = createStation();
        Charger charger = Charger.builder()
                .station(station)
                .chargerType(ChargerType.DC_FAST)
                .status(ChargerStatus.UNDER_MAINTENANCE)
                .pricePerKwh(BigDecimal.valueOf(0.50))
                .maintenanceNote("Needs repair")
                .build();
        charger = chargerRepository.save(charger);

        Map<String, String> payload = Map.of(
                "status", "AVAILABLE"
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().put("/api/chargers/{id}/status", charger.getId())
                .then().statusCode(200)
                .body("id", equalTo(charger.getId().intValue()))
                .body("status", equalTo("AVAILABLE"))
                .body("maintenanceNote", nullValue()); // Assert maintenance note is null

        // Verify the status was actually updated in the database and note cleared
        Charger updatedCharger = chargerRepository.findById(charger.getId()).orElse(null);
        assertThat(updatedCharger).isNotNull();
        assertThat(updatedCharger.getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
        assertThat(updatedCharger.getMaintenanceNote()).isNull();

        // Verify it's now included in available chargers
        given().when().get("/api/chargers/available")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.id == " + charger.getId() + " }.status", equalTo("AVAILABLE"));
    }
}
