package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MarkChargerAvailableStepDefs {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    private Long testChargerId;
    private static final AtomicInteger coordinateCounter = new AtomicInteger(0);

    @Given("a station with a charger that is {string} exists")
    public void a_station_with_a_charger_that_is_exists(String status) {
        chargerRepository.deleteAllInBatch();
        stationRepository.deleteAllInBatch();
        stationRepository.flush();

        int counter = coordinateCounter.getAndIncrement();

        Station station = new Station();
        station.setName("Test Station " + counter);
        station.setCity("Test City " + counter);
        station.setAddress("Test Address " + counter);
        station.setLatitude(40.0 + (counter * 1.0));
        station.setLongitude(-8.0 + (counter * 1.0));
        station = stationRepository.save(station);
        stationRepository.flush();

        Charger charger = new Charger();
        charger.setStation(station);
        charger.setChargerType(ChargerType.AC_STANDARD);
        charger.setStatus(ChargerStatus.valueOf(status.toUpperCase().replace(" ", "_")));
        if (status.equalsIgnoreCase("UNDER_MAINTENANCE")) {
             charger.setMaintenanceNote("Initial maintenance note");
        } else {
             charger.setMaintenanceNote(null);
        }
        charger.setPricePerKwh(BigDecimal.valueOf(0.30));
        charger = chargerRepository.save(charger);
        chargerRepository.flush();

        this.testChargerId = charger.getId();
    }

    @When("I mark the charger as {string}")
    public void i_mark_the_charger_as(String status) throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", status);

        ResultActions resultActions = mockMvc.perform(put("/api/chargers/{id}/status", testChargerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
        
        SharedContext.setLatestResultActions(resultActions);
    }

    @Then("the charger should be available for reservation")
    public void the_charger_should_be_available_for_reservation() {
        Optional<Charger> updatedChargerOpt = chargerRepository.findById(testChargerId);
        assertThat(updatedChargerOpt).isPresent();
        assertThat(updatedChargerOpt.get().getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
    }
} 