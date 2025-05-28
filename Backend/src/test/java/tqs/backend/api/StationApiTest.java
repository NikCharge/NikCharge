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
}
