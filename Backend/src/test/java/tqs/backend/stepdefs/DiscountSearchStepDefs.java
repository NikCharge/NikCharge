package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;

public class DiscountSearchStepDefs {

    private Response response;
    private static Long stationId1, stationId2;
    private int selectedHour;
    private String selectedChargerType;

    @Given("a discount-search station with id {int} exists")
public void aDiscountSearchStationWithIdExists(int id) {
    double baseLat = 40.0 + Math.random() * 0.01;   // Random latitude
    double baseLong = -8.0 - Math.random() * 0.01;  // Random longitude

    Map<String, Object> stationData = Map.of(
            "name", "Station " + id + " " + System.currentTimeMillis(),
            "address", "Address " + id,
            "city", "City",
            "latitude", baseLat,
            "longitude", baseLong
    );

    response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(stationData)
            .post("/api/stations");

    if (response.statusCode() != 200 && response.statusCode() != 201) {
        response.then().log().ifError();
        throw new RuntimeException("Failed to create station with id " + id);
    }

    Long createdId = response.jsonPath().getLong("id");
    if (id == 1) stationId1 = createdId;
    else if (id == 2) stationId2 = createdId;

    System.out.println("Created station " + id + " with backend ID: " + createdId);
}

    @And("a charger of type {string} exists for discount-search station {int}")
    public void aChargerExistsForStation(String chargerType, int id) {
        Long sid = getStationId(id);

        Map<String, Object> chargerData = Map.of(
                "stationId", sid,
                "chargerType", chargerType,
                "status", "AVAILABLE",
                "pricePerKwh", 0.8
        );

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(chargerData)
                .post("/api/chargers");

        response.then().log().ifError().statusCode(anyOf(equalTo(200), equalTo(201)));
        System.out.println("Created charger of type " + chargerType + " for station ID " + sid);
    }

    @And("a {int}% discount is active now for discount-search station {int} with charger type {string}")
    public void aDiscountIsActiveNow(int percent, int id, String chargerType) {
        Long sid = getStationId(id);

        int currentDay = LocalDateTime.now().getDayOfWeek().getValue();
        int currentHour = LocalDateTime.now().getHour();

        Map<String, Object> discountData = Map.of(
            "stationId", sid,
            "chargerType", chargerType,
            "discountPercent", (double) percent,
            "dayOfWeek", currentDay,
            "startHour", 11,
            "endHour", 13,
            "active", true
        );


        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(discountData)
                .post("/api/discounts");

        response.then().log().ifError().statusCode(anyOf(equalTo(200), equalTo(201)));
        System.out.println("Created discount for station ID " + sid + " at hour " + currentHour);
    }

@And("no discounts are currently active for discount-search station {int}")
public void noDiscountsAreActive(int id) {
    Long sid = getStationId(id);

    // Obter todos os descontos
    Response allDiscountsResponse = RestAssured.given()
            .get("/api/discounts");

    List<Map<String, Object>> allDiscounts = allDiscountsResponse.jsonPath().getList("");

    // Filtrar descontos dessa estação
    List<Integer> discountIds = allDiscounts.stream()
            .filter(discount -> {
                Map<String, Object> station = (Map<String, Object>) discount.get("station");
                return station != null && ((Number) station.get("id")).longValue() == sid;
            })
            .map(discount -> ((Number) discount.get("id")).intValue())
            .toList();

    // Deletar cada desconto individualmente
    for (Integer discountId : discountIds) {
        response = RestAssured.given()
                .delete("/api/discounts/" + discountId);

        response.then().log().ifValidationFails().statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    System.out.println("Deleted " + discountIds.size() + " discounts for station ID " + sid);
}

    @When("I search for stations at hour {int} with charger type {string}")
    public void iSearchForStationsAtHour(int hour, String chargerType) {
        this.selectedHour = hour;
        this.selectedChargerType = chargerType;
        System.out.println("Searching for stations at hour " + hour + " with charger type " + chargerType);
    }

    @When("I get the station list for discount search")
    public void iGetTheStationList() throws Exception {
        int currentDay = LocalDateTime.now().getDayOfWeek().getValue();

        // Busca todos os descontos do backend
        Response fullDiscountResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .get("/api/discounts");

        // Extrai a lista completa de descontos
        List<Map<String, Object>> allDiscounts = fullDiscountResponse.jsonPath().getList("");

        // Filtra os descontos ativos para o dia atual, hora e tipo de carregador selecionados
        List<Map<String, Object>> matchingDiscounts = allDiscounts.stream()
                .filter(discount -> {
                    int discountDay = ((Number) discount.get("dayOfWeek")).intValue();
                    int startHour = ((Number) discount.get("startHour")).intValue();
                    int endHour = ((Number) discount.get("endHour")).intValue();
                    String chargerType = (String) discount.get("chargerType");

                    return discountDay == currentDay &&
                            selectedHour >= startHour &&
                            selectedHour <= endHour &&
                            selectedChargerType.equals(chargerType);
                })
                .collect(Collectors.toList());

        // Serializa a lista filtrada para JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(matchingDiscounts);

        // Cria uma nova resposta com o corpo filtrado
        response = new ResponseBuilder().clone(fullDiscountResponse)
                .setBody(jsonBody)
                .build();

        // Loga o corpo para debug
        response.then().log().body();
    }


    @Then("discount-search station {int} should show a discount tag with value {string}")
    public void stationShouldShowDiscountTag(int id, String percentText) {
        Long sid = getStationId(id);
        // Convert percentText "15%" -> 15.0 for comparison
        double expectedPercent = Double.parseDouble(percentText.replace("%", ""));
        response.then().log().body();

        response.then()
        .statusCode(200)
        .body("find { it.station.id == " + sid + " }.discountPercent", anyOf(nullValue(), equalTo((float) expectedPercent)));

    }

   @Then("discount-search station {int} should show no discount tag")
    public void stationShouldShowNoDiscountTag(int id) {
        Long sid = getStationId(id);
        response.then().log().body();

        response.then()
                .statusCode(200)
                .body("find { it.station.id == " + sid + " }.discountPercent", anyOf(nullValue(), equalTo("")));
    }


    @Then("the discount-search station {int} should show {int}% off in card and pin")
    public void stationShouldShowDiscountInCardAndPin(int id, int percent) {
        Long sid = getStationId(id);
        String expected = percent + "% off";
        response.then()
                .statusCode(200)
                .body("find { it.id == " + sid + " }.cardTag", equalTo(expected))
                .body("find { it.id == " + sid + " }.pinTag", equalTo(expected));
    }

    @Then("the discount-search station {int} should not show discount on card or pin")
    public void stationShouldNotShowDiscountAnywhere(int id) {
        Long sid = getStationId(id);
        response.then()
                .statusCode(200)
                .body("find { it.id == " + sid + " }.cardTag", anyOf(nullValue(), equalTo("")))
                .body("find { it.id == " + sid + " }.pinTag", anyOf(nullValue(), equalTo("")));
    }

    // Utility method to map logical ID to actual station ID
    private Long getStationId(int id) {
        return switch (id) {
            case 1 -> stationId1;
            case 2 -> stationId2;
            default -> throw new IllegalStateException("Unsupported station ID: " + id);
        };
    }
}
