package tqs.backend.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ClientIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // ---------- SIGNUP TESTS ----------

    @Test
    void testSignupWithRealPostgres() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "TestUser",
                        "email", "testuser@example.com",
                        "password", "strongpass123",
                        "batteryCapacityKwh", 50,
                        "fullRangeKm", 300))
                .when().post("/api/clients/signup")
                .then().statusCode(200)
                .body("email", equalTo("testuser@example.com"));
    }

    @Test
    void testSignupWithDuplicateEmail_ReturnsConflict() {
        var data = Map.of(
                "name", "FirstUser",
                "email", "duplicate@example.com",
                "password", "strongpass123",
                "batteryCapacityKwh", 50,
                "fullRangeKm", 300
        );

        given().contentType(ContentType.JSON).body(data)
                .when().post("/api/clients/signup").then().statusCode(200);

        given().contentType(ContentType.JSON).body(data)
                .when().post("/api/clients/signup")
                .then().statusCode(409)
                .body("error", equalTo("Email already exists"));
    }

    @Test
    void testSignupWithInvalidEmail() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Invalid",
                        "email", "invalid-email",
                        "password", "abcdefgh",
                        "batteryCapacityKwh", 70,
                        "fullRangeKm", 350))
                .when().post("/api/clients/signup")
                .then().statusCode(400)
                .body("error.email", notNullValue());
    }

    // ---------- LOGIN TESTS ----------

    @Test
    void testValidLogin_ReturnsOk() {
        var signup = Map.of(
                "name", "LoginUser",
                "email", "loginuser@example.com",
                "password", "password123",
                "batteryCapacityKwh", 60,
                "fullRangeKm", 350
        );

        given().contentType(ContentType.JSON).body(signup)
                .when().post("/api/clients/signup").then().statusCode(200);

        var login = Map.of("email", "loginuser@example.com", "password", "password123");

        given().contentType(ContentType.JSON).body(login)
                .when().post("/api/clients/login")
                .then().statusCode(200)
                .body("email", equalTo("loginuser@example.com"))
                .body("name", equalTo("LoginUser"));
    }

    @Test
    void testLoginWithWrongPassword_ReturnsForbidden() {
        var signup = Map.of(
                "name", "WrongPass",
                "email", "wrongpass@example.com",
                "password", "correct123",
                "batteryCapacityKwh", 55,
                "fullRangeKm", 300
        );

        given().contentType(ContentType.JSON).body(signup)
                .when().post("/api/clients/signup").then().statusCode(200);

        given().contentType(ContentType.JSON)
                .body(Map.of("email", "wrongpass@example.com", "password", "wrong123"))
                .when().post("/api/clients/login")
                .then().statusCode(403)
                .body("error", equalTo("Invalid credentials"));
    }

    @Test
    void testLoginWithNonExistentEmail_ReturnsForbidden() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "notfound@example.com", "password", "somepass123"))
                .when().post("/api/clients/login")
                .then().statusCode(403)
                .body("error", equalTo("Invalid credentials"));
    }

    @Test
    void testLoginWithInvalidEmailFormat_ReturnsBadRequest() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "bad-format", "password", "abcdefg123"))
                .when().post("/api/clients/login")
                .then().statusCode(400)
                .body("error.email", notNullValue());
    }

    @Test
    void testLoginWithMissingFields_ReturnsBadRequest() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "missing@example.com"))
                .when().post("/api/clients/login")
                .then().statusCode(400)
                .body("error.password", notNullValue());
    }

    // ---------- UPDATE TESTS ----------

    @Test
    void testUpdateExistingClient_ReturnsUpdatedClient() {
        var signup = Map.of(
                "name", "Updater",
                "email", "update@example.com",
                "password", "updatepass",
                "batteryCapacityKwh", 50,
                "fullRangeKm", 300
        );

        given().contentType(ContentType.JSON).body(signup)
                .when().post("/api/clients/signup").then().statusCode(200);

        var update = Map.of(
                "name", "UpdatedName",
                "email", "newemail@example.com",
                "batteryCapacityKwh", 90,
                "fullRangeKm", 400
        );

        given().contentType(ContentType.JSON).body(update)
                .when().put("/api/clients/update@example.com")
                .then().statusCode(200)
                .body("email", equalTo("newemail@example.com"))
                .body("name", equalTo("UpdatedName"))
                .body("batteryCapacityKwh", equalTo(90.0f))
                .body("fullRangeKm", equalTo(400.0f));
    }

    @Test
    void testUpdateNonExistentClient_ReturnsNotFound() {
        var update = Map.of(
                "name", "Ghost",
                "email", "ghost@example.com",
                "batteryCapacityKwh", 80,
                "fullRangeKm", 350
        );

        given().contentType(ContentType.JSON).body(update)
                .when().put("/api/clients/ghost@example.com")
                .then().statusCode(404)
                .body("error", equalTo("Client not found"));
    }
}