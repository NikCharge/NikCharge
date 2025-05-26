// ClientIntegrationTest.java
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
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "FirstUser",
                        "email", "duplicate@example.com",
                        "password", "strongpass123",
                        "batteryCapacityKwh", 50,
                        "fullRangeKm", 300))
                .when().post("/api/clients/signup").then().statusCode(200);

        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "SecondUser",
                        "email", "duplicate@example.com",
                        "password", "strongpass123",
                        "batteryCapacityKwh", 60,
                        "fullRangeKm", 350))
                .when().post("/api/clients/signup")
                .then().statusCode(409)
                .body("error", equalTo("Email already exists"));
    }

    @Test
    void testSignupWithInvalidEmail() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Invalid",
                        "email", "invalid-email",
                        "password", "abcdefgh",
                        "batteryCapacityKwh", 70,
                        "fullRangeKm", 350))
                .when().post("/api/clients/signup")
                .then().statusCode(400)
                .body(containsString("email"));
    }

    @Test
    void testValidLogin_ReturnsOk() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "LoginUser",
                        "email", "loginuser@example.com",
                        "password", "password123",
                        "batteryCapacityKwh", 60,
                        "fullRangeKm", 350))
                .when().post("/api/clients/signup")
                .then().statusCode(200);

        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "loginuser@example.com",
                        "password", "password123"))
                .when().post("/api/clients/login")
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo("loginuser@example.com"))
                .body("name", equalTo("LoginUser"));
    }

    @Test
    void testLoginWithWrongPassword_ReturnsForbidden() {
        // Signup first
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "WrongPass",
                        "email", "wrongpass@example.com",
                        "password", "correct123",
                        "batteryCapacityKwh", 55,
                        "fullRangeKm", 300))
                .when().post("/api/clients/signup").then().statusCode(200);

        // Attempt login with wrong password
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "wrongpass@example.com",
                        "password", "wrong123"))
                .when().post("/api/clients/login")
                .then().statusCode(403)
                .body("error", equalTo("Invalid credentials"));
    }

    @Test
    void testLoginWithNonExistentEmail_ReturnsForbidden() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "notfound@example.com",
                        "password", "somepass123"))
                .when().post("/api/clients/login")
                .then().statusCode(403)
                .body("error", equalTo("Invalid credentials"));
    }

    @Test
    void testLoginWithMissingFields_ReturnsBadRequest() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "missing@example.com"))
                .when().post("/api/clients/login")
                .then().statusCode(400)
                .body("error.password", equalTo("Password is required"));
    }

}
