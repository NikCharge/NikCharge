package tqs.backend.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = StationIntegrationTest.Initializer.class)
public class StationIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                            "spring.datasource.url=" + postgres.getJdbcUrl(),
                            "spring.datasource.username=" + postgres.getUsername(),
                            "spring.datasource.password=" + postgres.getPassword(),
                            "spring.jpa.hibernate.ddl-auto=create-drop",
                            "spring.jpa.show-sql=true",
                            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect")
                    .applyTo(context.getEnvironment());
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // ---------- CREATE STATION TESTS ----------

    @Test
    void testCreateValidStation_ReturnsOk() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Test Station",
                        "address", "Rua Principal",
                        "city", "Aveiro",
                        "latitude", 40.633,
                        "longitude", -8.660))
                .when().post("/api/stations")
                .then().statusCode(200)
                .body("name", equalTo("Test Station"))
                .body("city", equalTo("Aveiro"));
    }

    @Test
    void testCreateStationWithDuplicateCoordinates_ReturnsConflict() {
        var payload = Map.of(
                "name", "Station 1",
                "address", "Rua 1",
                "city", "Aveiro",
                "latitude", 40.000,
                "longitude", -8.000
        );

        given().contentType(ContentType.JSON).body(payload)
                .when().post("/api/stations").then().statusCode(200);

        given().contentType(ContentType.JSON).body(payload)
                .when().post("/api/stations")
                .then().statusCode(409)
                .body("error", equalTo("Station already exists at this location"));
    }

    @Test
    void testCreateStationWithMissingCity_ReturnsBadRequest() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Incomplete Station",
                        "address", "Rua X",
                        "latitude", 41.0,
                        "longitude", -8.6))
                .when().post("/api/stations")
                .then().statusCode(400)
                .body("error.city", notNullValue());
    }

    // ---------- GET STATIONS TESTS ----------

    @Test
    void testGetAllStations_ReturnsList() {
        // Insert two stations
        var s1 = Map.of("name", "S1", "address", "A1", "city", "C1", "latitude", 41.0, "longitude", -8.0);
        var s2 = Map.of("name", "S2", "address", "A2", "city", "C2", "latitude", 42.0, "longitude", -9.0);

        given().contentType(ContentType.JSON).body(s1).when().post("/api/stations").then().statusCode(200);
        given().contentType(ContentType.JSON).body(s2).when().post("/api/stations").then().statusCode(200);

        given().when().get("/api/stations")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void testGetStationById_ReturnsStation() {
        var s = Map.of("name", "Unique", "address", "Rua Única", "city", "Cidade", "latitude", 43.0, "longitude", -9.1);

        // Create station and get ID
        int id = given().contentType(ContentType.JSON).body(s)
                .when().post("/api/stations")
                .then().statusCode(200)
                .extract().path("id");

        given().when().get("/api/stations/" + id)
                .then().statusCode(200)
                .body("name", equalTo("Unique"));
    }
}
