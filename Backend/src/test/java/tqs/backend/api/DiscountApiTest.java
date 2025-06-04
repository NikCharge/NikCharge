package tqs.backend.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.StationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;


import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DiscountApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private DiscountRepository discountRepository;

    private int stationCounter = 0;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        discountRepository.deleteAll();
        stationRepository.deleteAll();
    }

    private int createStation() {
        stationCounter++;

        var station = Map.of(
                "name", "Station " + stationCounter,
                "address", "Rua Estação " + stationCounter,
                "city", "Lisboa",
                "latitude", 38.722 + stationCounter * 0.01,
                "longitude", -9.139 - stationCounter * 0.01
        );

        return given().contentType(ContentType.JSON).body(station)
                .when().post("/api/stations")
                .then().statusCode(200)
                .extract().path("id");
    }

    @Test
    @DisplayName("POST /api/discounts - Criar desconto válido")
    void createValidDiscount_ReturnsOk() {
        int stationId = createStation();

        var discount = Map.of(
                "stationId", stationId,
                "chargerType", "AC_STANDARD",
                "dayOfWeek", 1,
                "startHour", 9,
                "endHour", 18,
                "discountPercent", 15.0,
                "active", true
        );

        given().contentType(ContentType.JSON).body(discount)
                .when().post("/api/discounts")
                .then().statusCode(200)
                .body("chargerType", equalTo("AC_STANDARD"))
                .body("dayOfWeek", equalTo(1))
                .body("discountPercent", equalTo(15.0f));
    }

    @Test
    @DisplayName("GET /api/discounts - Listar todos os descontos")
    void getAllDiscounts_ReturnsList() {
        int stationId = createStation();

        var discount = Map.of(
                "stationId", stationId,
                "chargerType", "DC_FAST",
                "dayOfWeek", 2,
                "startHour", 10,
                "endHour", 16,
                "discountPercent", 20.0,
                "active", true
        );

        given().contentType(ContentType.JSON).body(discount)
                .when().post("/api/discounts")
                .then().statusCode(200);

        given().when().get("/api/discounts")
                .then().statusCode(200)
                .body("$", not(empty()));
    }

    @Test
    @DisplayName("GET /api/discounts/{id} - Buscar desconto por ID")
    void getDiscountById_ReturnsDiscount() {
        int stationId = createStation();

        var discount = Map.of(
                "stationId", stationId,
                "chargerType", "DC_ULTRA_FAST",
                "dayOfWeek", 3,
                "startHour", 8,
                "endHour", 14,
                "discountPercent", 10.0,
                "active", true
        );

        int discountId = given().contentType(ContentType.JSON).body(discount)
                .when().post("/api/discounts")
                .then().statusCode(200)
                .extract().path("id");

        given().when().get("/api/discounts/" + discountId)
                .then().statusCode(200)
                .body("id", equalTo(discountId));
    }

    @Test
    @DisplayName("PUT /api/discounts/{id} - Atualizar desconto")
    void updateDiscount_ReturnsUpdated() {
        int stationId = createStation();

        var discount = Map.of(
                "stationId", stationId,
                "chargerType", "AC_STANDARD",
                "dayOfWeek", 4,
                "startHour", 12,
                "endHour", 20,
                "discountPercent", 5.0,
                "active", true
        );

        int discountId = given().contentType(ContentType.JSON).body(discount)
                .when().post("/api/discounts")
                .then().statusCode(200)
                .extract().path("id");

        var updatedDiscount = Map.of(
                "stationId", stationId,
                "chargerType", "DC_FAST",
                "dayOfWeek", 5,
                "startHour", 14,
                "endHour", 22,
                "discountPercent", 25.0,
                "active", false
        );

        given().contentType(ContentType.JSON).body(updatedDiscount)
                .when().put("/api/discounts/" + discountId)
                .then().statusCode(200)
                .body("chargerType", equalTo("DC_FAST"))
                .body("discountPercent", equalTo(25.0f));
    }

    @Test
    @DisplayName("DELETE /api/discounts/{id} - Apagar desconto existente")
    void deleteDiscount_ReturnsNoContent() {
        int stationId = createStation();

        var discount = Map.of(
                "stationId", stationId,
                "chargerType", "AC_STANDARD",
                "dayOfWeek", 6,
                "startHour", 10,
                "endHour", 15,
                "discountPercent", 30.0,
                "active", true
        );

        int discountId = given().contentType(ContentType.JSON).body(discount)
                .when().post("/api/discounts")
                .then().statusCode(200)
                .extract().path("id");

        given().when().delete("/api/discounts/" + discountId)
                .then().statusCode(204);
    }

    @Test
@DisplayName("GET /api/discounts/{id} - Retornar 404 se desconto não existir")
void getDiscountByInvalidId_ReturnsNotFound() {
    given().when().get("/api/discounts/999999")
            .then().statusCode(404)
            .body("message", equalTo("Discount not found"));
}

@Test
@DisplayName("PUT /api/discounts/{id} - Retornar 404 se desconto não existir")
void updateInvalidDiscount_ReturnsNotFound() {
    int stationId = createStation();

    var updatedDiscount = Map.of(
            "stationId", stationId,
            "chargerType", "AC_STANDARD",
            "dayOfWeek", 1,
            "startHour", 8,
            "endHour", 18,
            "discountPercent", 10.0,
            "active", true
    );

    given().contentType(ContentType.JSON).body(updatedDiscount)
            .when().put("/api/discounts/999999")
            .then().statusCode(404)
            .body("message", equalTo("Discount not found"));
}

@Test
@DisplayName("DELETE /api/discounts/{id} - Retornar 404 se desconto não existir")
void deleteInvalidDiscount_ReturnsNotFound() {
    given().when().delete("/api/discounts/999999")
            .then().statusCode(404)
            .body("message", equalTo("Discount not found"));
}

@Test
@DisplayName("POST /api/discounts - Retornar 404 se estação não existir")
void createDiscountWithInvalidStation_ReturnsNotFound() {
    var discount = Map.of(
            "stationId", 999999,
            "chargerType", "AC_STANDARD",
            "dayOfWeek", 1,
            "startHour", 9,
            "endHour", 18,
            "discountPercent", 15.0,
            "active", true
    );

    given().contentType(ContentType.JSON).body(discount)
            .when().post("/api/discounts")
            .then().statusCode(404)
            .body("message", equalTo("Station not found"));
}



}
