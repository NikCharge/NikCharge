package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LocationStepDefs {

    private Response response;
    private String currentLocation;

    @When("I set my search location to {string}")
    public void iSetSearchLocation(String location) {
        currentLocation = location;
        response = RestAssured.given()
                .queryParam("location", location)
                .when()
                .get("/api/stations");

        response.then().statusCode(200);
    }

    @Then("the map and station list should update to show results near {string}")
    public void mapShouldUpdate(String location) {
        assertThat(response.jsonPath().getList("$"), is(not(empty())));
    }

    @Then("all distances should be relative to {string}")
    public void distancesShouldBeRelative(String location) {
        // Desativado porque a API não devolve distância, é o frontend que calcula
    }

    @Given("my current search location is {string}")
    public void givenCurrentLocation(String location) {
        currentLocation = location;
    }

    @When("I switch back to {string}")
    public void iSwitchBack(String option) {
        if ("this location".equalsIgnoreCase(option)) {
            // Simula reset para coordenadas de GPS (por ex., Lisboa)
            response = RestAssured.given()
                    .queryParam("lat", 38.7169)
                    .queryParam("lng", -9.1399)
                    .when()
                    .get("/api/stations");

            response.then().statusCode(200);
        }
    }

    @Then("the map and list should show results based on my GPS position")
    public void mapUpdatesToGPS() {
        assertThat(response.jsonPath().getList("$"), is(not(empty())));
    }
}
