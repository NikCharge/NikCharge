package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StationFilteringStepDefs {

    private Response response;

    @Given("there are multiple stations with chargers of various types")
    public void setupStationsWithVariousChargers() {
        // Pré-condição: assume que os dados já foram criados via script ou setup externo
        // Este passo é mais para clareza do teste e integração com ambiente populado
    }

    @When("I filter stations by charger type {string}")
    public void iFilterByChargerType(String chargerType) {
        response = RestAssured.given()
                .queryParam("lat", 40.6333)
                .queryParam("lng", -8.659)
                .when()
                .get("/api/stations");

        response.then().statusCode(200);
    }

    @Then("I should only see stations that have {string} chargers")
    public void iSeeOnlyStationsWithChargerType(String expectedType) {
        List<Map<String, Object>> stations = response.jsonPath().getList("$");

        for (Map<String, Object> station : stations) {
            List<Map<String, Object>> chargers = (List<Map<String, Object>>) station.get("chargers");

            List<String> types = chargers.stream()
                    .map(c -> c.get("chargerType").toString())
                    .toList();

            assertThat(
                    "Each station should contain the expected charger type",
                    types,
                    hasItem(expectedType)
            );
        }
    }
}
