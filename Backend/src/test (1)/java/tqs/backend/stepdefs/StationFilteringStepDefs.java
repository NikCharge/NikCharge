package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class StationFilteringStepDefs {

    private Response response;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Given("there are multiple stations with chargers of various types")
    public void setupStationsWithVariousChargers() {
        // Clean up existing data
        chargerRepository.deleteAll();
        stationRepository.deleteAll();

        // Create stations with different charger types
        Station station1 = new Station();
        station1.setName("Station 1");
        station1.setAddress("Address 1");
        station1.setCity("City 1");
        station1.setLatitude(40.6333);
        station1.setLongitude(-8.659);
        station1 = stationRepository.save(station1);

        Station station2 = new Station();
        station2.setName("Station 2");
        station2.setAddress("Address 2");
        station2.setCity("City 2");
        station2.setLatitude(40.6334);
        station2.setLongitude(-8.660);
        station2 = stationRepository.save(station2);

        // Add chargers to stations
        Charger acCharger = new Charger();
        acCharger.setStation(station1);
        acCharger.setChargerType(ChargerType.AC_STANDARD);
        acCharger.setStatus(ChargerStatus.AVAILABLE);
        acCharger.setPricePerKwh(BigDecimal.valueOf(0.20));
        chargerRepository.save(acCharger);

        Charger dcFastCharger = new Charger();
        dcFastCharger.setStation(station2);
        dcFastCharger.setChargerType(ChargerType.DC_FAST);
        dcFastCharger.setStatus(ChargerStatus.AVAILABLE);
        dcFastCharger.setPricePerKwh(BigDecimal.valueOf(0.30));
        chargerRepository.save(dcFastCharger);

        Charger dcUltraCharger = new Charger();
        dcUltraCharger.setStation(station1);
        dcUltraCharger.setChargerType(ChargerType.DC_ULTRA_FAST);
        dcUltraCharger.setStatus(ChargerStatus.AVAILABLE);
        dcUltraCharger.setPricePerKwh(BigDecimal.valueOf(0.40));
        chargerRepository.save(dcUltraCharger);
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
            
            // Skip stations without chargers
            if (chargers == null || chargers.isEmpty()) {
                continue;
            }

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
