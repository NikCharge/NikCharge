package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.controller.ChargerController;
import tqs.backend.model.Charger;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.service.ChargerService;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;
import tqs.backend.model.Station;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.annotation.DirtiesContext;
import java.util.Map;
import java.util.HashMap;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChargerDashboardStepDefs {

    @Autowired
    private ChargerService chargerService;

    @Autowired
    private ChargerController chargerController;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private StationRepository stationRepository;

    private List<Charger> chargerList;
    private ResponseEntity<?> response;
    private Station testStation;
    private Map<String, Long> chargerIdMap = new HashMap<>();

    @Given("I am logged in as a station employee")
    public void iAmLoggedInAsStationEmployee() {
        // Authentication logic would go here
    }

    @And("I am viewing my station's dashboard")
    public void iAmViewingMyStationsDashboard() {
        // Clean up existing data
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
        chargerIdMap.clear();
        
        // Create new station with random coordinates
        testStation = new Station();
        testStation.setName("Test Station");
        testStation.setAddress("Test Address");
        testStation.setCity("Test City");
        testStation.setLatitude(Math.random() * 180 - 90); // Random latitude between -90 and 90
        testStation.setLongitude(Math.random() * 360 - 180); // Random longitude between -180 and 180
        testStation = stationRepository.saveAndFlush(testStation);
        assertNotNull(testStation.getId(), "Station should be saved successfully");

        // Create a default charger for the station
        Charger defaultCharger = new Charger();
        defaultCharger.setStatus(ChargerStatus.AVAILABLE);
        defaultCharger.setStation(testStation);
        defaultCharger = chargerRepository.saveAndFlush(defaultCharger);
        assertNotNull(defaultCharger.getId(), "Default charger should be saved successfully");
        chargerIdMap.put("CH001", defaultCharger.getId());
    }

    @Given("there is a charger with ID {string}")
    public void thereIsAChargerWithId(String chargerId) {
        Charger charger = new Charger();
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStation(testStation);
        charger = chargerRepository.saveAndFlush(charger);
        System.out.println("Saved charger: " + charger.getId() + ", station: " + (charger.getStation() != null ? charger.getStation().getId() : null));
        assertNotNull(charger.getId(), "Charger ID should be generated");
        assertNotNull(charger.getStation(), "Charger should be associated with a station");
        chargerIdMap.put(chargerId, charger.getId());
    }

    @Given("there is a charger with ID {string} that is currently in use")
    public void thereIsAChargerInUse(String chargerId) {
        Charger charger = new Charger();
        charger.setStatus(ChargerStatus.IN_USE);
        charger.setStation(testStation);
        charger = chargerRepository.saveAndFlush(charger);
        System.out.println("Saved charger: " + charger.getId() + ", station: " + (charger.getStation() != null ? charger.getStation().getId() : null));
        assertNotNull(charger.getId(), "Charger ID should be generated");
        assertNotNull(charger.getStation(), "Charger should be associated with a station");
        chargerIdMap.put(chargerId, charger.getId());
    }

    @Given("there is a charger with ID {string} that is under maintenance")
    public void thereIsAChargerUnderMaintenance(String chargerId) {
        Charger charger = new Charger();
        charger.setStatus(ChargerStatus.UNDER_MAINTENANCE);
        charger.setStation(testStation);
        charger = chargerRepository.saveAndFlush(charger);
        System.out.println("Saved charger: " + charger.getId() + ", station: " + (charger.getStation() != null ? charger.getStation().getId() : null));
        assertNotNull(charger.getId(), "Charger ID should be generated");
        assertNotNull(charger.getStation(), "Charger should be associated with a station");
        chargerIdMap.put(chargerId, charger.getId());
    }

    @Given("there are multiple chargers at my station")
    public void thereAreMultipleChargersAtMyStation() {
        // Create multiple chargers with different statuses
        Charger charger1 = new Charger();
        charger1.setStatus(ChargerStatus.AVAILABLE);
        charger1.setStation(testStation);
        charger1 = chargerRepository.saveAndFlush(charger1);
        System.out.println("Saved charger1: " + charger1.getId() + ", station: " + (charger1.getStation() != null ? charger1.getStation().getId() : null));
        assertNotNull(charger1.getId(), "Charger 1 ID should be generated");
        assertNotNull(charger1.getStation(), "Charger 1 should be associated with a station");

        Charger charger2 = new Charger();
        charger2.setStatus(ChargerStatus.IN_USE);
        charger2.setStation(testStation);
        charger2 = chargerRepository.saveAndFlush(charger2);
        System.out.println("Saved charger2: " + charger2.getId() + ", station: " + (charger2.getStation() != null ? charger2.getStation().getId() : null));
        assertNotNull(charger2.getId(), "Charger 2 ID should be generated");
        assertNotNull(charger2.getStation(), "Charger 2 should be associated with a station");

        Charger charger3 = new Charger();
        charger3.setStatus(ChargerStatus.UNDER_MAINTENANCE);
        charger3.setStation(testStation);
        charger3 = chargerRepository.saveAndFlush(charger3);
        System.out.println("Saved charger3: " + charger3.getId() + ", station: " + (charger3.getStation() != null ? charger3.getStation().getId() : null));
        assertNotNull(charger3.getId(), "Charger 3 ID should be generated");
        assertNotNull(charger3.getStation(), "Charger 3 should be associated with a station");
    }

    @When("I request the charger status list")
    public void iRequestTheChargerStatusList() {
        response = chargerController.getChargersByStation(testStation.getId());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        chargerList = (List<Charger>) response.getBody();
        assertNotNull(chargerList, "Response body should not be null");
    }

    @Then("I should see a list of all chargers at my station")
    public void iShouldSeeListOfAllChargers() {
        assertNotNull(chargerList, "Charger list should not be null");
        assertTrue(chargerList.size() > 0, "Charger list should not be empty");
        // Verify that all chargers belong to the test station
        chargerList.forEach(charger -> {
            assertNotNull(charger.getStation(), "Charger should have a station");
            assertEquals(testStation.getId(), charger.getStation().getId(), "Charger should belong to the test station");
        });
    }

    @Then("I should see charger {string} with status {string}")
    public void iShouldSeeChargerWithStatus(String chargerId, String status) {
        assertNotNull(chargerList, "Charger list should not be null");
        Long actualId = chargerIdMap.get(chargerId);
        assertNotNull(actualId, "Charger ID mapping should exist for " + chargerId);
        
        Charger foundCharger = chargerList.stream()
                .filter(c -> c.getId().equals(actualId))
                .findFirst()
                .orElse(null);
        
        assertNotNull(foundCharger, "Charger with ID " + chargerId + " should be found");
        assertEquals(ChargerStatus.valueOf(status.toUpperCase().replace(" ", "_")), 
                    foundCharger.getStatus(),
                    "Charger status should be " + status);
    }

    @Then("I should see all chargers with their respective statuses")
    public void iShouldSeeAllChargersWithTheirStatuses() {
        assertNotNull(chargerList, "Charger list should not be null");
        assertTrue(chargerList.size() >= 3, "Should have at least 3 chargers");
        
        boolean hasAvailable = chargerList.stream()
                .anyMatch(c -> c.getStatus() == ChargerStatus.AVAILABLE);
        boolean hasInUse = chargerList.stream()
                .anyMatch(c -> c.getStatus() == ChargerStatus.IN_USE);
        boolean hasUnderMaintenance = chargerList.stream()
                .anyMatch(c -> c.getStatus() == ChargerStatus.UNDER_MAINTENANCE);

        assertTrue(hasAvailable, "Should have at least one available charger");
        assertTrue(hasInUse, "Should have at least one in-use charger");
        assertTrue(hasUnderMaintenance, "Should have at least one charger under maintenance");
    }
} 