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
import tqs.backend.model.enums.ChargerType;
import tqs.backend.service.ChargerService;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;
import tqs.backend.model.Station;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.annotation.DirtiesContext;

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
    private Map<ChargerStatus, Long> statusCounts = new HashMap<>();

    @Given("I am logged in as a station employee")
    public void iAmLoggedInAsStationEmployee() {
        // Authentication logic would go here
    }

    @And("I am viewing the stations dashboard")
    public void iAmViewingTheStationsDashboard() {
        // Clean up existing data
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
        chargerIdMap.clear();
        statusCounts.clear();
        
        // Create multiple stations with random coordinates
        for (int i = 1; i <= 3; i++) {
            Station station = new Station();
            station.setName("Test Station " + i);
            station.setAddress("Test Address " + i);
            station.setCity("Test City " + i);
            station.setLatitude(Math.random() * 180 - 90);
            station.setLongitude(Math.random() * 360 - 180);
            station = stationRepository.saveAndFlush(station);
            assertNotNull(station.getId(), "Station " + i + " should be saved successfully");

            // Create a default charger for each station
            Charger defaultCharger = new Charger();
            defaultCharger.setStatus(ChargerStatus.AVAILABLE);
            defaultCharger.setStation(station);
            defaultCharger.setChargerType(ChargerType.AC_STANDARD);
            defaultCharger.setPricePerKwh(BigDecimal.valueOf(0.25));
            defaultCharger = chargerRepository.saveAndFlush(defaultCharger);
            assertNotNull(defaultCharger.getId(), "Default charger for station " + i + " should be saved successfully");
            chargerIdMap.put("CH00" + i, defaultCharger.getId());
        }

        // Set the first station as the test station for scenarios that need a specific station
        testStation = stationRepository.findAll().get(0);
    }

    @Given("there is a charger with ID {string}")
    public void thereIsAChargerWithId(String chargerId) {
        Charger charger = new Charger();
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStation(testStation);
        charger.setChargerType(ChargerType.AC_STANDARD);
        charger.setPricePerKwh(BigDecimal.valueOf(0.25));
        charger = chargerRepository.saveAndFlush(charger);
        assertNotNull(charger.getId(), "Charger ID should be generated");
        assertNotNull(charger.getStation(), "Charger should be associated with a station");
        chargerIdMap.put(chargerId, charger.getId());
    }

    @Given("there is a charger with ID {string} that is currently in use")
    public void thereIsAChargerInUse(String chargerId) {
        Charger charger = new Charger();
        charger.setStatus(ChargerStatus.IN_USE);
        charger.setStation(testStation);
        charger.setChargerType(ChargerType.DC_FAST);
        charger.setPricePerKwh(BigDecimal.valueOf(0.35));
        charger = chargerRepository.saveAndFlush(charger);
        assertNotNull(charger.getId(), "Charger ID should be generated");
        assertNotNull(charger.getStation(), "Charger should be associated with a station");
        chargerIdMap.put(chargerId, charger.getId());
    }

    @Given("there is a charger with ID {string} that is under maintenance")
    public void thereIsAChargerUnderMaintenance(String chargerId) {
        Charger charger = new Charger();
        charger.setStatus(ChargerStatus.UNDER_MAINTENANCE);
        charger.setStation(testStation);
        charger.setChargerType(ChargerType.DC_ULTRA_FAST);
        charger.setPricePerKwh(BigDecimal.valueOf(0.45));
        charger = chargerRepository.saveAndFlush(charger);
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
        charger1.setChargerType(ChargerType.AC_STANDARD);
        charger1.setPricePerKwh(BigDecimal.valueOf(0.25));
        charger1 = chargerRepository.saveAndFlush(charger1);
        assertNotNull(charger1.getId(), "Charger 1 ID should be generated");
        assertNotNull(charger1.getStation(), "Charger 1 should be associated with a station");

        Charger charger2 = new Charger();
        charger2.setStatus(ChargerStatus.IN_USE);
        charger2.setStation(testStation);
        charger2.setChargerType(ChargerType.DC_FAST);
        charger2.setPricePerKwh(BigDecimal.valueOf(0.35));
        charger2 = chargerRepository.saveAndFlush(charger2);
        assertNotNull(charger2.getId(), "Charger 2 ID should be generated");
        assertNotNull(charger2.getStation(), "Charger 2 should be associated with a station");

        Charger charger3 = new Charger();
        charger3.setStatus(ChargerStatus.UNDER_MAINTENANCE);
        charger3.setStation(testStation);
        charger3.setChargerType(ChargerType.DC_ULTRA_FAST);
        charger3.setPricePerKwh(BigDecimal.valueOf(0.45));
        charger3 = chargerRepository.saveAndFlush(charger3);
        assertNotNull(charger3.getId(), "Charger 3 ID should be generated");
        assertNotNull(charger3.getStation(), "Charger 3 should be associated with a station");
    }

    @When("I request the charger status list")
    public void iRequestTheChargerStatusList() {
        assertNotNull(testStation, "Test station should not be null");
        assertNotNull(testStation.getId(), "Test station ID should not be null");
        
        response = chargerController.getChargersByStation(testStation.getId());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        
        Object responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        
        if (responseBody instanceof List<?>) {
            chargerList = (List<Charger>) responseBody;
            assertNotNull(chargerList, "Charger list should not be null");
            
            // Calculate status counts
            statusCounts.clear();
            for (ChargerStatus status : ChargerStatus.values()) {
                long count = chargerList.stream()
                        .filter(c -> c.getStatus() == status)
                        .count();
                statusCounts.put(status, count);
            }
        } else {
            fail("Response body should be a List<Charger>");
        }
    }

    @When("I filter chargers by status {string}")
    public void iFilterChargersByStatus(String status) {
        assertNotNull(chargerList, "Charger list must be initialized before filtering");
        ChargerStatus filterStatus = ChargerStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        chargerList = chargerList.stream()
                .filter(c -> c.getStatus() == filterStatus)
                .toList();
    }

    @When("I sort chargers by status")
    public void iSortChargersByStatus() {
        assertNotNull(chargerList, "Charger list must be initialized before sorting");
        chargerList = chargerList.stream()
                .sorted(Comparator.comparing(c -> c.getStatus().name()))
                .toList();
    }

    @Then("I should see a list of all chargers at my station")
    public void iShouldSeeListOfAllChargers() {
        assertNotNull(chargerList, "Charger list should not be null");
        assertTrue(chargerList.size() > 0, "Charger list should not be empty");
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

    @Then("I should see the total count of available chargers")
    public void iShouldSeeTotalCountOfAvailableChargers() {
        assertNotNull(statusCounts, "Status counts should not be null");
        assertTrue(statusCounts.getOrDefault(ChargerStatus.AVAILABLE, 0L) > 0, 
                "Should have at least one available charger");
    }

    @Then("I should see the total count of in-use chargers")
    public void iShouldSeeTotalCountOfInUseChargers() {
        assertNotNull(statusCounts, "Status counts should not be null");
        assertTrue(statusCounts.getOrDefault(ChargerStatus.IN_USE, 0L) > 0, 
                "Should have at least one in-use charger");
    }

    @Then("I should see the total count of chargers under maintenance")
    public void iShouldSeeTotalCountOfChargersUnderMaintenance() {
        assertNotNull(statusCounts, "Status counts should not be null");
        assertTrue(statusCounts.getOrDefault(ChargerStatus.UNDER_MAINTENANCE, 0L) > 0, 
                "Should have at least one charger under maintenance");
    }

    @Then("I should only see chargers with status {string}")
    public void iShouldOnlySeeChargersWithStatus(String status) {
        ChargerStatus expectedStatus = ChargerStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        assertNotNull(chargerList, "Charger list should not be null");
        assertTrue(chargerList.size() > 0, "Should have at least one charger");
        
        chargerList.forEach(charger -> 
            assertEquals(expectedStatus, charger.getStatus(), 
                    "All chargers should have status " + status));
    }

    @Then("the chargers should be sorted by status alphabetically")
    public void chargersShouldBeSortedByStatus() {
        assertNotNull(chargerList, "Charger list should not be null");
        assertTrue(chargerList.size() > 1, "Should have at least two chargers to verify sorting");
        
        for (int i = 0; i < chargerList.size() - 1; i++) {
            String currentStatus = chargerList.get(i).getStatus().name();
            String nextStatus = chargerList.get(i + 1).getStatus().name();
            assertTrue(currentStatus.compareTo(nextStatus) <= 0, 
                    "Chargers should be sorted by status alphabetically");
        }
    }

    @And("each charger should have a valid ID and status")
    public void eachChargerShouldHaveValidIdAndStatus() {
        assertNotNull(chargerList, "Charger list should not be null");
        chargerList.forEach(charger -> {
            assertNotNull(charger.getId(), "Charger should have an ID");
            assertNotNull(charger.getStatus(), "Charger should have a status");
        });
    }

    @Then("the charger should have a valid price per kWh")
    public void theChargerShouldHaveAValidPricePerKWh() {
        assertNotNull(chargerList, "Charger list should not be null");
        chargerList.forEach(charger -> {
            assertNotNull(charger.getPricePerKwh(), "Charger should have a price per kWh");
            assertTrue(charger.getPricePerKwh().compareTo(BigDecimal.ZERO) > 0, 
                    "Price per kWh should be greater than zero");
        });
    }

    @Then("each charger should have a valid price per kWh")
    public void eachChargerShouldHaveAValidPricePerKWh() {
        assertNotNull(chargerList, "Charger list should not be null");
        chargerList.forEach(charger -> {
            assertNotNull(charger.getPricePerKwh(), "Charger should have a price per kWh");
            assertTrue(charger.getPricePerKwh().compareTo(BigDecimal.ZERO) > 0, 
                    "Price per kWh should be greater than zero");
            assertTrue(charger.getPricePerKwh().compareTo(BigDecimal.valueOf(1.0)) <= 0,
                    "Price per kWh should not exceed 1.0");
            
            // Check if price is reasonable based on charger type
            if (charger.getChargerType() == ChargerType.AC_STANDARD) {
                assertTrue(charger.getPricePerKwh().compareTo(BigDecimal.valueOf(0.30)) <= 0,
                        "AC Standard charger price should not exceed 0.30");
            } else if (charger.getChargerType() == ChargerType.DC_FAST) {
                assertTrue(charger.getPricePerKwh().compareTo(BigDecimal.valueOf(0.50)) <= 0,
                        "DC Fast charger price should not exceed 0.50");
            } else if (charger.getChargerType() == ChargerType.DC_ULTRA_FAST) {
                assertTrue(charger.getPricePerKwh().compareTo(BigDecimal.valueOf(0.70)) <= 0,
                        "DC Ultra Fast charger price should not exceed 0.70");
            }
        });
    }
} 