package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TimeSelectionStepDefs {

    private Response response;
    private String selectedTime;

    @Given("there are charging stations with available chargers")
    public void thereAreStationsWithAvailableChargers() {
        Response creationResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("""
                    {
                        "name": "Test Future Station",
                        "location": "Futuria",
                        "latitude": 40.6400,
                        "longitude": -8.6500,
                        "description": "Station for time testing",
                        "imageUrl": "",
                        "address": "Rua do Tempo",
                        "city": "Futuria"
                    }
                """)
                .post("/api/stations");

        creationResponse.then().statusCode(anyOf(is(200), is(201), is(409)));

        response = RestAssured.get("/api/stations");
        response.then().statusCode(200);
        List<?> stations = response.jsonPath().getList("$");

        assertThat("Expected at least one station", stations, is(not(empty())));
    }

    @When("the user opens the calendar picker")
    public void userOpensCalendarPicker() {
        // Este passo é apenas de UI/UX; pode ser um placeholder para contexto
        // Nenhuma ação de backend necessária aqui
    }

    @When("selects {string}")
    public void selectsCustomFutureTime(String futureTimeDescription) {
        // Simula uma data futura baseada na descrição
        if (futureTimeDescription.equalsIgnoreCase("tomorrow at 10:00")) {
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            selectedTime = futureTime.format(formatter);

            response = RestAssured.given()
                    .queryParam("lat", 40.6400)
                    .queryParam("lng", -8.6500)
                    .queryParam("datetime", selectedTime)
                    .when()
                    .get("/api/stations");

            response.then().statusCode(200);
        }
    }

    @Then("the station availability should update to reflect that selected time")
    public void availabilityReflectsFutureTime() {
        List<Map<String, Object>> stations = response.jsonPath().getList("$");
        assertThat("Expected stations to be returned for future time", stations, is(not(empty())));
    }

    @When("the user selects the option {string}")
    public void userSelectsCurrentTime(String option) {
        if (option.equalsIgnoreCase("Current time")) {
            response = RestAssured.given()
                    .queryParam("lat", 40.6400)
                    .queryParam("lng", -8.6500)
                    .when()
                    .get("/api/stations");

            response.then().statusCode(200);
        }
    }

    @Then("the availability should update to show real-time data")
    public void availabilityIsRealtime() {
        List<Map<String, Object>> stations = response.jsonPath().getList("$");
        assertThat("Expected stations for real-time availability", stations, is(not(empty())));
        // Possível futura verificação de estado em tempo real
    }
}
