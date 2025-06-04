package tqs.backend.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChargerApiTest {

    @LocalServerPort
    private int port;

    private int stationCounter = 0;
    
    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        stationRepository.deleteAll();
        chargerRepository.deleteAll();
    }

    private int createStation() {
        stationCounter++;

        double latitude = 38.722 + stationCounter * 0.01;
        double longitude = -9.139 - stationCounter * 0.01;

        var station = Map.of(
                "name", "Charger Station " + stationCounter,
                "address", "Rua Charger " + stationCounter,
                "city", "Porto",
                "latitude", latitude,
                "longitude", longitude
        );

        return given().contentType(ContentType.JSON).body(station)
                .when().post("/api/stations")
                .then().statusCode(200)
                .extract().path("id");
    }

    @Test
    @DisplayName("POST /api/chargers - Create valid charger")
    void postValidCharger_ReturnsOk() {
        int stationId = createStation();

        var charger = Map.of(
                "stationId", stationId,
                "chargerType", "AC_STANDARD",
                "status", "AVAILABLE",
                "pricePerKwh", 0.25
        );

        given().contentType(ContentType.JSON).body(charger)
                .when().post("/api/chargers")
                .then()
                .statusCode(200)
                .body("chargerType", equalTo("AC_STANDARD"))
                .body("status", equalTo("AVAILABLE"))
                .body("pricePerKwh", equalTo(0.25f));
    }

    @Test
    @DisplayName("GET /api/chargers - Get all chargers")
    void getAllChargers_ReturnsList() {
        int stationId = createStation();

        var charger = Map.of(
                "stationId", stationId,
                "chargerType", "DC_FAST",
                "status", "AVAILABLE",
                "pricePerKwh", 0.40
        );

        given().contentType(ContentType.JSON).body(charger)
                .when().post("/api/chargers")
                .then().statusCode(200);

        given()
                .when().get("/api/chargers")
                .then().statusCode(200)
                .body("$", not(empty()));
    }

    @Test
    @DisplayName("GET /api/chargers/station/{id} - Get chargers for a station")
    void getChargersByStation_ReturnsOnlyChargersFromStation() {
        int stationId = createStation();

        var charger1 = Map.of(
                "stationId", stationId,
                "chargerType", "AC_STANDARD",
                "status", "AVAILABLE",
                "pricePerKwh", 0.20
        );

        given().contentType(ContentType.JSON).body(charger1)
                .when().post("/api/chargers")
                .then().statusCode(200);

        given()
                .when().get("/api/chargers/station/" + stationId)
                .then().statusCode(200)
                .body("$", not(empty()))
                .body("findAll { it.chargerType == 'AC_STANDARD' }.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("DELETE /api/chargers/{id} - Delete existing charger")
    void deleteCharger_ReturnsNoContent() {
        int stationId = createStation();

        var charger = Map.of(
                "stationId", stationId,
                "chargerType", "AC_STANDARD",
                "status", "AVAILABLE",
                "pricePerKwh", 0.25
        );

        int chargerId = given().contentType(ContentType.JSON).body(charger)
                .when().post("/api/chargers")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .when().delete("/api/chargers/" + chargerId)
                .then().statusCode(204);

        given()
                .when().get("/api/chargers/station/" + stationId)
                .then().statusCode(200)
                .body("id", not(hasItem(chargerId)));
    }

    @Test
    @DisplayName("DELETE /api/chargers/{id} - Delete nonexistent charger returns 404")
    void deleteNonexistentCharger_ReturnsNotFound() {
        given()
                .when().delete("/api/chargers/999999")
                .then().statusCode(404)
                .body("error", equalTo("Charger not found"));
    }
}
