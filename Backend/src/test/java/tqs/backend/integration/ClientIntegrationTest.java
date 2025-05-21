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
@ContextConfiguration(initializers = ClientIntegrationTest.Initializer.class)
public class ClientIntegrationTest {

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

    @Test
    void testSignupWithRealPostgres() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "TestUser",
                        "email", "testuser@example.com",
                        "password", "strongpass123",
                        "batteryCapacityKwh", 50,
                        "fullRangeKm", 300))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(200)
                .body("email", equalTo("testuser@example.com"));
    }

    @Test
    void testSignupWithDuplicateEmail_ReturnsConflict() {
        // First signup
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "FirstUser",
                        "email", "duplicate@example.com",
                        "password", "strongpass123",
                        "batteryCapacityKwh", 50,
                        "fullRangeKm", 300))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(200);

        // Second signup with same email
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "SecondUser",
                        "email", "duplicate@example.com",
                        "password", "strongpass123",
                        "batteryCapacityKwh", 60,
                        "fullRangeKm", 350))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(409);
    }

    @Test
    void testSignupWithInvalidData_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "InvalidUser",
                        "email", "invalid-email",
                        "password", "123", // Too short
                        "batteryCapacityKwh", -1, // Invalid negative value
                        "fullRangeKm", -1)) // Invalid negative value
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400);
    }

    @Test
    void testSignupWithMissingRequiredFields_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "IncompleteUser",
                        "email", "incomplete@example.com"))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400);
    }

    @Test
    void testSignupWithExtremeValues_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "ExtremeUser",
                        "email", "extreme@example.com",
                        "password", "strongpass123",
                        "batteryCapacityKwh", 1000, // Unrealistically high
                        "fullRangeKm", 10000)) // Unrealistically high
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400);
    }
}
