package tqs.backend.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ChargerIntegrationTest.Initializer.class)
public class ChargerIntegrationTest {

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
        // Limpar DB antes de cada teste
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
    }

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    // Helper para criar uma estação no banco para usar nos testes
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
                "stationId", 999999L,  // Estação que não existe
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
                .status(ChargerStatus.IN_USE)
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

    // ---------- DELETE CHARGER ----------

    @Test
    void testDeleteCharger_ExistingId_ReturnsNoContent() {
        Station station = createStation();

        Charger charger = Charger.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.25))
                .build();

        Charger saved = chargerRepository.save(charger);

        given().when().delete("/api/chargers/{id}", saved.getId())
                .then().statusCode(204);
    }

    @Test
    void testDeleteCharger_NonExistingId_ReturnsNotFound() {
        given().when().delete("/api/chargers/{id}", 999999L)
                .then().statusCode(404)
                .body("error", containsString("Charger not found"));
    }
}
