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
        Station station = Station.builder()
                .name("Integration Test Station")
                .address("Rua Teste")
                .city("Aveiro")
                .latitude(40.63)
                .longitude(-8.66)
                .build();
        return stationRepository.save(station);
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
}
