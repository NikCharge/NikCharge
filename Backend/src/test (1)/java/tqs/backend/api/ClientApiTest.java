package tqs.backend.api;

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
class ClientApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // ---------- SIGNUP TESTS ----------

    @Test
    void postValidSignup_ReturnsOk() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Rita",
                        "email", "rita@nik.pt",
                        "password", "abcdefgh",
                        "batteryCapacityKwh", 70,
                        "fullRangeKm", 350))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(200)
                .body("email", equalTo("rita@nik.pt"));
    }

    @Test
    void postSignupWithInvalidEmail_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Invalid",
                        "email", "invalid-email",
                        "password", "abcdefgh",
                        "batteryCapacityKwh", 70,
                        "fullRangeKm", 350))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400)
                .body("error.email", notNullValue());
    }

    @Test
    void postSignupWithMissingFields_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Incomplete",
                        "email", "incomplete@example.com",
                        "password", "abcdefgh"))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400)
                .body("error.batteryCapacityKwh", notNullValue());
    }

    @Test
    void postSignupWithInvalidBatteryCapacity_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Invalid",
                        "email", "invalid@example.com",
                        "password", "abcdefgh",
                        "batteryCapacityKwh", -1,
                        "fullRangeKm", 350))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400)
                .body("error.batteryCapacityKwh", notNullValue());
    }

    @Test
    void postSignupWithInvalidRange_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Invalid",
                        "email", "invalid@example.com",
                        "password", "abcdefgh",
                        "batteryCapacityKwh", 70,
                        "fullRangeKm", -1))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400)
                .body("error.fullRangeKm", notNullValue());
    }

    @Test
    void postSignupWithWeakPassword_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Weak",
                        "email", "weak@example.com",
                        "password", "123",
                        "batteryCapacityKwh", 70,
                        "fullRangeKm", 350))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400)
                .body("error.password", notNullValue());
    }

    @Test
    void postSignupWithDuplicateEmail_ReturnsConflict() {
        var data = Map.of(
                "name", "Dupe",
                "email", "dupe@example.com",
                "password", "abcdefgh",
                "batteryCapacityKwh", 70,
                "fullRangeKm", 350
        );

        // First sign-up should succeed
        given().contentType(ContentType.JSON)
                .body(data).when().post("/api/clients/signup")
                .then().statusCode(200);

        // Second sign-up should fail
        given().contentType(ContentType.JSON)
                .body(data).when().post("/api/clients/signup")
                .then().statusCode(409)
                .body("error", equalTo("Email already exists"));
    }

    // ---------- LOGIN TESTS ----------

    @Test
    void postValidLogin_ReturnsOk() {
        var signUp = Map.of(
                "name", "LoginUser",
                "email", "login@example.com",
                "password", "abcdefgh",
                "batteryCapacityKwh", 70,
                "fullRangeKm", 350
        );

        given().contentType(ContentType.JSON).body(signUp)
                .when().post("/api/clients/signup").then().statusCode(200);

        var login = Map.of("email", "login@example.com", "password", "abcdefgh");

        given().contentType(ContentType.JSON).body(login)
                .when().post("/api/clients/login")
                .then().statusCode(200)
                .body("email", equalTo("login@example.com"))
                .body("name", equalTo("LoginUser"));
    }

    @Test
    void postLoginWithWrongPassword_ReturnsForbidden() {
        var login = Map.of("email", "login@example.com", "password", "wrongpass");

        given().contentType(ContentType.JSON)
                .body(login)
                .when().post("/api/clients/login")
                .then().statusCode(403)
                .body("error", equalTo("Invalid credentials"));
    }

    @Test
    void postLoginWithNonExistentEmail_ReturnsForbidden() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "notfound@example.com", "password", "abcdefgh"))
                .when().post("/api/clients/login")
                .then().statusCode(403)
                .body("error", equalTo("Invalid credentials"));
    }

    @Test
    void postLoginWithInvalidEmailFormat_ReturnsBadRequest() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "invalidemail", "password", "abcdefgh"))
                .when().post("/api/clients/login")
                .then().statusCode(400)
                .body("error.email", notNullValue());
    }

    @Test
    void postLoginWithMissingFields_ReturnsBadRequest() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "login@example.com"))
                .when().post("/api/clients/login")
                .then().statusCode(400)
                .body("error.password", notNullValue());
    }

    // ---------- UPDATE CLIENT TESTS ----------

    @Test
    void putUpdateExistingClient_ReturnsUpdatedData() {
        var email = "update@example.com";
        var original = Map.of(
                "name", "Original",
                "email", email,
                "password", "abcdefgh",
                "batteryCapacityKwh", 60,
                "fullRangeKm", 300
        );

        var updated = Map.of(
                "name", "Updated",
                "email", "updated@example.com",
                "batteryCapacityKwh", 80,
                "fullRangeKm", 400
        );

        // Create user
        given().contentType(ContentType.JSON).body(original)
                .when().post("/api/clients/signup")
                .then().statusCode(200);

        // Update
        given().contentType(ContentType.JSON).body(updated)
                .when().put("/api/clients/" + email)
                .then().statusCode(200)
                .body("email", equalTo("updated@example.com"))
                .body("name", equalTo("Updated"));
    }

    @Test
    void putUpdateNonExistentClient_ReturnsNotFound() {
        var update = Map.of(
                "name", "Updated",
                "email", "updated@example.com",
                "batteryCapacityKwh", 80,
                "fullRangeKm", 400
        );

        given().contentType(ContentType.JSON).body(update)
                .when().put("/api/clients/nonexistent@example.com")
                .then().statusCode(404)
                .body("error", equalTo("Client not found"));
    }
}
