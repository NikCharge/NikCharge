package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class DiscountSearchStepDefs {

    private Response response;
    private static Long stationId1, stationId2;

    private int selectedHour;
    private String selectedChargerType;

    @Given("a discount-search station with id {int} exists")
    public void aDiscountSearchStationWithIdExists(int id) {
        Map<String, Object> stationData = Map.of(
                "name", "Station " + id,
                "address", "Address " + id,
                "city", "City",
                "latitude", 40.0 + id,
                "longitude", -8.0
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(stationData)
                .post("/api/stations");

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            Long createdId = response.jsonPath().getLong("id");
            if (id == 1) stationId1 = createdId;
            if (id == 2) stationId2 = createdId;
        }
    }

    @And("a {int}% discount is active now for discount-search station {int} with charger type {string}")
    public void aDiscountSearchDiscountIsActive(int percent, int id, String chargerType) {
        Long sid = (id == 1) ? stationId1 : stationId2;
        int currentDay = LocalDateTime.now().getDayOfWeek().getValue() % 7;
        int currentHour = LocalDateTime.now().getHour();

        Map<String, Object> discountData = Map.of(
                "stationId", sid,
                "chargerType", chargerType,
                "discountPercent", percent,
                "dayOfWeek", currentDay,
                "startHour", currentHour - 1,
                "endHour", currentHour + 1,
                "active", true
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(discountData)
                .post("/api/discounts");

        response.then().statusCode(anyOf(equalTo(200), equalTo(201)));
    }

    @And("no discounts are currently active for discount-search station {int}")
    public void noDiscountSearchDiscountsAreActive(int id) {
        // Expand here if needed to clean up test state
    }

    @When("I search for stations at hour {int} with charger type {string}")
    public void iSearchForStationsAtTimeWithChargerType(int hour, String chargerType) {
        this.selectedHour = hour;
        this.selectedChargerType = chargerType;
    }

    @When("I get the station list for discount search")
    public void iGetStationListForDiscountSearch() {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("hour", selectedHour)
                .queryParam("chargerType", selectedChargerType)
                .get("/api/stations");
    }

    @Then("discount-search station {int} should show a discount tag with value {string}")
    public void discountSearchStationShouldHaveTag(int id, String percentText) {
        Long sid = (id == 1) ? stationId1 : stationId2;
        response.then()
                .statusCode(200)
                .body("find { it.id == %s }.discount".formatted(sid), equalTo(Integer.parseInt(percentText)));
    }

    @Then("discount-search station {int} should show no discount tag")
    public void discountSearchStationShouldHaveNoTag(int id) {
        Long sid = (id == 1) ? stationId1 : stationId2;
        response.then()
                .statusCode(200)
                .body("find { it.id == %s }.discount".formatted(sid), anyOf(nullValue(), equalTo(null)));
    }

    @Then("the discount-search station {int} should show {int}% off in card and pin")
    public void discountSearchStationCardAndPinShowDiscount(int id, int percent) {
        Long sid = (id == 1) ? stationId1 : stationId2;
        response.then()
                .statusCode(200)
                .body("find { it.id == %s }.cardTag".formatted(sid), equalTo("%d%% off".formatted(percent)))
                .body("find { it.id == %s }.pinTag".formatted(sid), equalTo("%d%% off".formatted(percent)));
    }

    @Then("the discount-search station {int} should not show discount on card or pin")
    public void discountSearchStationCardAndPinShowNothing(int id) {
        Long sid = (id == 1) ? stationId1 : stationId2;
        response.then()
                .statusCode(200)
                .body("find { it.id == %s }.cardTag".formatted(sid), anyOf(nullValue(), equalTo("")))
                .body("find { it.id == %s }.pinTag".formatted(sid), anyOf(nullValue(), equalTo("")));
    }
}
