package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LocationStepDefs {

    private Response response;

    @Given("there are charging stations in the system")
    public void chargingStationsExist() {
        // Criar estação via API
        Response creationResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("""
            {
                "name": "Test Aveiro Station",
                "location": "Aveiro",
                "latitude": 40.6333,
                "longitude": -8.659,
                "description": "For test",
                "imageUrl": "",
                "address": "Universidade de Aveiro",
                "city": "Aveiro"
            }
            """)
                .post("/api/stations");

        creationResponse.then().statusCode(anyOf(is(200), is(201), is(409))); // 409 se já existir

        // Verificar se existem estações agora
        response = RestAssured.get("/api/stations");
        response.then().statusCode(200);
        List<?> stations = response.jsonPath().getList("$");

        assertThat("At least one station should exist in the system", stations, is(not(empty())));
    }


    @When("the user sets the location to {string}")
    public void userSetsSearchLocation(String location) {
        response = RestAssured.given()
                .queryParam("location", location)
                .when()
                .get("/api/stations");

        response.then().statusCode(200);
    }

    @Then("the map and station list should include at least one station")
    public void mapShouldUpdate() {
        List<Map<String, Object>> stations = response.jsonPath().getList("$");
        assertThat("Expected at least one station in the response", stations, is(not(empty())));
    }

    @When("the user resets the location to use GPS")
    public void userResetsToGPS() {
        response = RestAssured.given()
                .queryParam("lat", 38.7169)
                .queryParam("lng", -9.1399)
                .when()
                .get("/api/stations");

        response.then().statusCode(200);
    }

    @Then("the map and list should include at least one station")
    public void mapUpdatesToGPS() {
        List<Map<String, Object>> stations = response.jsonPath().getList("$");
        assertThat("Expected at least one station when using GPS", stations, is(not(empty())));
    }
}
