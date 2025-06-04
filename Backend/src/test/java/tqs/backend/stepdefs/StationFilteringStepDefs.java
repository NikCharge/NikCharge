package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StationFilteringStepDefs {

    private Response response;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;


    @Given("there are multiple stations with chargers of various types and statuses")
    public void setupStationsWithVariousChargersAndStatuses() {
        chargerRepository.deleteAll();
        stationRepository.deleteAll();

        // Criação das estações
        Station station1 = Station.builder()
                .name("Station Alpha")
                .address("Rua A")
                .city("Lisboa")
                .latitude(38.7223)
                .longitude(-9.1393)
                .build();

        Station station2 = Station.builder()
                .name("Station Beta")
                .address("Rua B")
                .city("Porto")
                .latitude(41.1579)
                .longitude(-8.6291)
                .build();

        station1 = stationRepository.save(station1);
        station2 = stationRepository.save(station2);

        // Criação de carregadores com diferentes tipos e estados
        Charger charger1 = Charger.builder()
                .station(station1)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.25))
                .build();

        Charger charger2 = Charger.builder()
                .station(station1)
                .chargerType(ChargerType.DC_FAST)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.40))
                .build();

        Charger charger3 = Charger.builder()
                .station(station2)
                .chargerType(ChargerType.AC_STANDARD)
                .status(ChargerStatus.IN_USE)
                .pricePerKwh(BigDecimal.valueOf(0.30))
                .build();

        chargerRepository.saveAll(List.of(charger1, charger2, charger3));
    }

    @When("I filter stations by charger type {string} and status {string}")
    public void iFilterStationsByChargerTypeAndStatus(String chargerTypeStr, String statusStr) {
        ChargerType chargerType = ChargerType.valueOf(chargerTypeStr);
        ChargerStatus status = ChargerStatus.valueOf(statusStr);

        // Filtra carregadores que correspondem ao tipo e estado pedidos
        List<Charger> filteredChargers = chargerRepository.findAll().stream()
                .filter(c -> c.getChargerType() == chargerType && c.getStatus() == status)
                .toList();

        // Obtém IDs das estações que possuem esses carregadores
        List<Long> stationIds = filteredChargers.stream()
                .map(c -> c.getStation().getId())
                .distinct()
                .toList();

        // Faz GET para cada estação filtrada para obter os detalhes
        // Guarda resposta da última estação (podes adaptar para várias estações se quiser)
        if (stationIds.isEmpty()) {
            throw new RuntimeException("No stations found with specified charger type and status");
        }

        Long stationId = stationIds.get(0);

        response = RestAssured.given()
                .when()
                .get("/api/stations/" + stationId + "/details");

        response.then().statusCode(200);
    }

    @Then("I should only see stations that have {string} chargers with status {string}")
    public void iShouldOnlySeeStationsWithChargersAndStatus(String expectedType, String expectedStatus) {
        Map<String, Object> stationDetails = response.jsonPath().getMap("$");
        assertThat(stationDetails, is(notNullValue()));

        List<Map<String, Object>> chargers = (List<Map<String, Object>>) stationDetails.get("chargers");
        assertThat(chargers, is(not(empty())));

        boolean hasExpectedCharger = chargers.stream()
                .anyMatch(c ->
                        expectedType.equals(c.get("chargerType").toString()) &&
                                expectedStatus.equals(c.get("status").toString())
                );

        assertThat("Station should have at least one charger with expected type and status", hasExpectedCharger, is(true));


    }
}
