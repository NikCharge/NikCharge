package tqs.backend.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StationApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    

    @Test
    @DisplayName("POST /api/stations - Create valid station")
    void postValidStation_ReturnsOk() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Station A",
                        "address", "Rua Principal",
                        "city", "Aveiro",
                        "latitude", 40.633,
                        "longitude", -8.660
                ))
                .when()
                .post("/api/stations")
                .then()
                .statusCode(200)
                .body("name", equalTo("Station A"))
                .body("city", equalTo("Aveiro"));
    }

    @Test
    @DisplayName("POST /api/stations - Duplicate coordinates returns 409")
    void postDuplicateStation_ReturnsConflict() {
        var station = Map.of(
                "name", "Station B",
                "address", "Rua X",
                "city", "Aveiro",
                "latitude", 40.631,
                "longitude", -8.661
        );

        given().contentType(ContentType.JSON).body(station)
                .when().post("/api/stations")
                .then().statusCode(200);

        given().contentType(ContentType.JSON).body(station)
                .when().post("/api/stations")
                .then().statusCode(409)
                .body("error", equalTo("Station already exists at this location"));
    }

    @Test
    @DisplayName("POST /api/stations - Missing fields returns 400")
    void postStationMissingFields_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "No City",
                        "latitude", 40.633,
                        "longitude", -8.660
                ))
                .when()
                .post("/api/stations")
                .then()
                .statusCode(400)
                .body("error.city", notNullValue());
    }

    @Test
    @DisplayName("GET /api/stations - Fetch all stations")
    void getAllStations_ReturnsList() {
        given()
                .when()
                .get("/api/stations")
                .then()
                .statusCode(200)
                .body("$", not(empty()));
    }

    @Test
    @DisplayName("GET /api/stations/{id} - Fetch specific station")
    void getStationById_ReturnsCorrectData() {
        var station = Map.of(
                "name", "Station C",
                "address", "Rua Y",
                "city", "Porto",
                "latitude", 40.634,
                "longitude", -8.662
        );

        int id = given().contentType(ContentType.JSON).body(station)
                .when().post("/api/stations")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .when()
                .get("/api/stations/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("city", equalTo("Porto"));
    }

        @Test
        @DisplayName("GET /api/stations/{id}/details - Detailed station info with chargers and availability")
        void getStationDetails_ReturnsDetailedInfo() {
        var station = Map.of(
                "name", "Station D",
                "address", "Rua Z",
                "city", "Lisboa",
                "latitude", 38.722,
                "longitude", -9.139
        );

        int stationId = given().contentType(ContentType.JSON).body(station)
                .when().post("/api/stations")
                .then().statusCode(200)
                .extract().path("id");

        var charger1 = Map.of(
                "stationId", stationId,
                "chargerType", "DC_FAST",
                "status", "AVAILABLE",
                "pricePerKwh", 0.30
        );
        var charger2 = Map.of(
                "stationId", stationId,
                "chargerType", "AC_STANDARD",
                "status", "IN_USE",
                "pricePerKwh", 0.20
        );

        given().contentType(ContentType.JSON).body(charger1)
                .when().post("/api/chargers")
                .then().statusCode(anyOf(is(200), is(201)));

        given().contentType(ContentType.JSON).body(charger2)
                .when().post("/api/chargers")
                .then().statusCode(anyOf(is(200), is(201)));

        given()
                .when()
                .get("/api/stations/" + stationId + "/details")
                .then()
                .statusCode(200)
                .body("name", equalTo("Station D"))
                .body("address", equalTo("Rua Z"))
                .body("chargers", not(empty()))
                .body("chargers.findAll { it.chargerType == 'DC_FAST' }.size()", greaterThanOrEqualTo(1))
                .body("chargers.findAll { it.chargerType == 'AC_STANDARD' }.size()", greaterThanOrEqualTo(1))
                .body("chargers[0].pricePerKwh", notNullValue())
                .body("chargers[0].status", anyOf(equalTo("AVAILABLE"), equalTo("IN_USE"), equalTo("MAINTENANCE")));
        }

        @Test
        @DisplayName("DELETE /api/stations/{id} - Delete existing station returns 204")
        void deleteExistingStation_ReturnsNoContent() {
        var station = Map.of(
                "name", "Station To Delete",
                "address", "Rua Delete",
                "city", "Coimbra",
                "latitude", 40.207,
                "longitude", -8.429
        );

        int id = given().contentType(ContentType.JSON).body(station)
                .when().post("/api/stations")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .when()
                .delete("/api/stations/" + id)
                .then()
                .statusCode(204);

        // Verifica que ao tentar buscar a estação deletada retorna 404
        given()
                .when()
                .get("/api/stations/" + id)
                .then()
                .statusCode(404)
                .body("error", equalTo("Station not found"));
        }

        @Test
        @DisplayName("DELETE /api/stations/{id} - Deleting non-existing station returns 404")
        void deleteNonExistingStation_ReturnsNotFound() {
        long nonExistingId = 999999L;

        given()
                .when()
                .delete("/api/stations/" + nonExistingId)
                .then()
                .statusCode(404)
                .body("error", notNullValue());
        }


        }
