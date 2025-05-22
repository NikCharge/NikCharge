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

    @Test
    void postValidSignup_ReturnsOk() {
        RestAssured
                .given()
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
        RestAssured
                .given()
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
                .statusCode(400);
    }

    @Test
    void postSignupWithMissingFields_ReturnsBadRequest() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Incomplete",
                        "email", "incomplete@example.com",
                        "password", "abcdefgh"))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(400);
    }

    @Test
    void postSignupWithInvalidBatteryCapacity_ReturnsBadRequest() {
        RestAssured
                .given()
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
                .statusCode(400);
    }

    @Test
    void postSignupWithInvalidRange_ReturnsBadRequest() {
        RestAssured
                .given()
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
                .statusCode(400);
    }

    @Test
    void postSignupWithWeakPassword_ReturnsBadRequest() {
        RestAssured
                .given()
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
                .statusCode(400);
    }

    @Test
    void postValidLogin_ReturnsOk() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "LoginUser",
                        "email", "login@example.com",
                        "password", "abcdefgh",
                        "batteryCapacityKwh", 70,
                        "fullRangeKm", 350))
                .when().post("/api/clients/signup").then().statusCode(200);

        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "login@example.com",
                        "password", "abcdefgh"))
                .when().post("/api/clients/login")
                .then().statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void postLoginWithWrongPassword_ReturnsForbidden() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "login@example.com",
                        "password", "wrongpass"))
                .when().post("/api/clients/login")
                .then().statusCode(403);
    }

    @Test
    void postLoginWithNonExistentEmail_ReturnsForbidden() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "notfound@example.com",
                        "password", "abcdefgh"))
                .when().post("/api/clients/login")
                .then().statusCode(403);
    }

    @Test
    void postLoginWithInvalidEmailFormat_ReturnsBadRequest() {
        given().contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "invalidemail",
                        "password", "abcdefgh"))
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
}
