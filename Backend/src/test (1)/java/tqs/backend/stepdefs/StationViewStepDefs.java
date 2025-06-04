package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import java.util.Map;

import static org.hamcrest.Matchers.*;

public class StationViewStepDefs {

    private Response response;
    private static Long createdStationId = null;

    @Given("a station with id {int} exists in the system")
    public void aStationWithIdExistsInTheSystem(int id) {
        // Only create the station if we haven't already
        if (createdStationId == null) {
            Map<String, Object> stationData = Map.of(
                    "name", "Test Station",
                    "address", "Test Address",
                    "city", "Test City",
                    "latitude", 40.0,
                    "longitude", -8.0);

            response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(stationData)
                    .post("/api/stations");

            if (response.statusCode() == 200) {
                createdStationId = response.jsonPath().getLong("id");
            } else if (response.statusCode() == 409) {
                // Station already exists, fetch its ID
                Response getResponse = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .get("/api/stations");
                getResponse.then().statusCode(200);
                // Find the station with the same coordinates
                createdStationId = getResponse.jsonPath()
                        .getLong("find { it.latitude == 40.0 && it.longitude == -8.0 }.id");
            } else {
                throw new AssertionError("Unexpected response when creating station: " + response.statusLine());
            }
        }
    }

    @When("I request the list of stations")
    public void iRequestTheListOfStations() {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .get("/api/stations");
    }

    @Then("I should receive a list of stations")
    public void iShouldReceiveAListOfStations() {
        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()));
    }

    @When("I request the station with id {int}")
    public void iRequestTheStationWithId(int id) {
        // Use the created station's ID if available and id==1
        long stationId = (id == 1 && createdStationId != null) ? createdStationId : id;
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .get("/api/stations/" + stationId);
    }

    @Then("I should receive the station details for id {int}")
    public void iShouldReceiveTheStationDetailsForId(int id) {
        long stationId = (id == 1 && createdStationId != null) ? createdStationId : id;
        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo((int) stationId))
                .body("name", notNullValue())
                .body("address", notNullValue())
                .body("city", notNullValue())
                .body("latitude", notNullValue())
                .body("longitude", notNullValue());
    }

    @Then("the station view should fail with status {int}")
    public void theStationViewShouldFailWithStatus(int status) {
        response.then()
                .statusCode(status)
                .contentType(ContentType.JSON);
    }

    @And("I should receive an error message about station not found")
    public void iShouldReceiveAnErrorMessageAboutStationNotFound() {
        response.then()
                .body("error", equalTo("Station not found"));
    }
}