package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StationFilteringStepDefs {

    private List<Map<String, Object>> stationList;

    @Given("three stations with two of type {string}, one available and one in use, and one of a different type")
    public void createThreeStationsWithTwoOfSameTypeAndOneDifferent(String commonType) {
        createStationWithCharger("Station A", commonType, "AVAILABLE", 38.72, -9.14);   // Deve ser selecionada
        createStationWithCharger("Station B", commonType, "IN_USE", 38.73, -9.15);      // Ignorada (IN_USE)
        createStationWithCharger("Station C", "AC_STANDARD", "AVAILABLE", 38.74, -9.16); // Ignorada (tipo diferente)
    }

    private void createStationWithCharger(String name, String chargerType, String status, double lat, double lng) {
        Map<String, Object> stationData = Map.of(
                "name", name,
                "address", "Rua Exemplo",
                "city", "Lisboa",
                "latitude", lat,
                "longitude", lng
        );

        Response stationRes = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(stationData)
                .post("/api/stations");

        Long stationId;
        if (stationRes.statusCode() == 200 || stationRes.statusCode() == 201) {
            stationId = stationRes.jsonPath().getLong("id");
        } else if (stationRes.statusCode() == 409) {
            stationRes = RestAssured.given()
                    .get("/api/stations");
            stationId = stationRes.jsonPath()
                    .getLong("find { it.name == '" + name + "' }.id");
        } else {
            throw new RuntimeException("Erro ao criar estação: " + name);
        }

        Map<String, Object> chargerData = Map.of(
                "stationId", stationId,
                "chargerType", chargerType,
                "status", status,
                "pricePerKwh", 1.0
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(chargerData)
                .post("/api/chargers")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)));
    }

    @When("I fetch and filter stations for charger type {string}")
    public void fetchAndFilterStationsForChargerType(String chargerType) {
        Response baseRes = RestAssured.given().get("/api/stations");
        List<Map<String, Object>> baseStations = baseRes.jsonPath().getList("$");

        stationList = baseStations.stream().map(station -> {
            Long id = Long.parseLong(String.valueOf(station.get("id")));
            Response detailsRes = RestAssured.given().get("/api/stations/" + id + "/details");
            Map<String, Object> details = detailsRes.jsonPath().getMap("$");
            return Map.of(
                    "id", id,
                    "name", details.get("name"),
                    "chargers", details.get("chargers")
            );
        }).filter(station -> {
            List<Map<String, Object>> chargers = (List<Map<String, Object>>) station.get("chargers");
            return chargers.stream().anyMatch(c ->
                    chargerType.equals(c.get("chargerType")) &&
                    "AVAILABLE".equals(c.get("status"))
            );
        }).toList();
    }

    @Then("only the available station with type {string} is selected")
    public void onlyAvailableStationOfTypeIsSelected(String expectedType) {
        assertThat("Apenas uma estação deve ser selecionada", stationList.size(), is(1));

        Map<String, Object> selectedStation = stationList.get(0);
        String name = (String) selectedStation.get("name");
        List<Map<String, Object>> chargers = (List<Map<String, Object>>) selectedStation.get("chargers");

        assertThat("A estação selecionada deve ser a 'Station A'", name, equalTo("Station A"));

        assertThat("Deve conter carregador do tipo correto e disponível",
                chargers.stream().anyMatch(c ->
                        expectedType.equals(c.get("chargerType")) &&
                        "AVAILABLE".equals(c.get("status"))
                ),
                is(true)
        );
    }
}
