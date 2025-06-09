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
import tqs.backend.model.Discount;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.StationRepository;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = DiscountIntegrationTest.Initializer.class)
public class DiscountIntegrationTest {

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
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
                    "stripe.api.key=sk_test_dummy"
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        discountRepository.deleteAll();
        stationRepository.deleteAll();
    }

    private Station createStation() {
        Station station = Station.builder()
                .name("Station A")
                .address("Rua X")
                .city("Porto")
                .latitude(41.15)
                .longitude(-8.61)
                .build();
        return stationRepository.save(station);
    }

    // ---------- CREATE DISCOUNT ----------

    @Test
    void testCreateDiscount_ValidRequest_ReturnsOk() {
        Station station = createStation();

        Map<String, Object> payload = Map.of(
                "stationId", station.getId(),
                "chargerType", "AC_STANDARD",
                "dayOfWeek", 1,
                "startHour", 8,
                "endHour", 12,
                "discountPercent", 10.0,
                "active", true
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().post("/api/discounts")
                .then().statusCode(200)
                .body("id", notNullValue())
                .body("chargerType", equalTo("AC_STANDARD"))
                .body("dayOfWeek", equalTo(1))
                .body("discountPercent", equalTo(10.0f));
    }

    @Test
    void testCreateDiscount_InvalidStation_ReturnsNotFound() {
        Map<String, Object> payload = Map.of(
                "stationId", 9999L,
                "chargerType", "DC_FAST",
                "dayOfWeek", 2,
                "startHour", 10,
                "endHour", 16,
                "discountPercent", 15.0,
                "active", false
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().post("/api/discounts")
                .then().statusCode(404)
                .body("message", containsString("Station not found"));
    }

    // ---------- GET ALL DISCOUNTS ----------

    @Test
    void testGetAllDiscounts_ReturnsList() {
        Station station = createStation();

        Discount d1 = Discount.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .dayOfWeek(3)
                .startHour(7)
                .endHour(11)
                .discountPercent(5.0)
                .active(true)
                .build();

        Discount d2 = Discount.builder()
                .station(station)
                .chargerType(ChargerType.DC_FAST)
                .dayOfWeek(4)
                .startHour(14)
                .endHour(18)
                .discountPercent(20.0)
                .active(false)
                .build();

        discountRepository.saveAll(List.of(d1, d2));

        given().when().get("/api/discounts")
                .then().statusCode(200)
                .body("size()", equalTo(2));
    }

    // ---------- GET DISCOUNT BY ID ----------

    @Test
    void testGetDiscountById_ValidId_ReturnsDiscount() {
        Station station = createStation();

        Discount discount = Discount.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .dayOfWeek(5)
                .startHour(9)
                .endHour(13)
                .discountPercent(12.5)
                .active(true)
                .build();

        Discount saved = discountRepository.save(discount);

        given().when().get("/api/discounts/{id}", saved.getId())
                .then().statusCode(200)
                .body("id", equalTo(saved.getId().intValue()))
                .body("discountPercent", equalTo(12.5f));
    }

    @Test
    void testGetDiscountById_NotFound_ReturnsNotFound() {
        given().when().get("/api/discounts/{id}", 99999L)
                .then().statusCode(404)
                .body("message", containsString("Discount not found"));
    }

    // ---------- UPDATE DISCOUNT ----------

    @Test
    void testUpdateDiscount_ValidRequest_ReturnsUpdated() {
        Station station = createStation();

        Discount discount = Discount.builder()
                .station(station)
                .chargerType(ChargerType.DC_FAST)
                .dayOfWeek(2)
                .startHour(6)
                .endHour(9)
                .discountPercent(8.0)
                .active(true)
                .build();

        Discount saved = discountRepository.save(discount);

        Map<String, Object> payload = Map.of(
                "stationId", station.getId(),
                "chargerType", "DC_FAST",
                "dayOfWeek", 2,
                "startHour", 6,
                "endHour", 10,
                "discountPercent", 12.0,
                "active", false
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().put("/api/discounts/{id}", saved.getId())
                .then().statusCode(200)
                .body("discountPercent", equalTo(12.0f))
                .body("active", equalTo(false));
    }

    @Test
    void testUpdateDiscount_NotFound_ReturnsNotFound() {
        Map<String, Object> payload = Map.of(
                "stationId", 1L,
                "chargerType", "AC_STANDARD",
                "dayOfWeek", 1,
                "startHour", 8,
                "endHour", 10,
                "discountPercent", 5.0,
                "active", true
        );

        given().contentType(ContentType.JSON)
                .body(payload)
                .when().put("/api/discounts/{id}", 9999L)
                .then().statusCode(404)
                .body("message", containsString("Discount not found"));
    }

    // ---------- DELETE DISCOUNT ----------

    @Test
    void testDeleteDiscount_ValidId_ReturnsNoContent() {
        Station station = createStation();

        Discount discount = Discount.builder()
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .dayOfWeek(6)
                .startHour(10)
                .endHour(12)
                .discountPercent(7.5)
                .active(true)
                .build();

        Discount saved = discountRepository.save(discount);

        given().when().delete("/api/discounts/{id}", saved.getId())
                .then().statusCode(204);
    }

    @Test
    void testDeleteDiscount_NotFound_ReturnsNotFound() {
        given().when().delete("/api/discounts/{id}", 9999L)
                .then().statusCode(404)
                .body("message", containsString("Discount not found"));
    }
} 