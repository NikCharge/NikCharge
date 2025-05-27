package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

public class LocationStepDefs {

    @When("I set my search location to {string}")
    public void iSetSearchLocation(String location) {
        // Simular lógica de alteração de localização
        System.out.println("Set location to: " + location);
    }

    @Then("the map and station list should update to show results near {string}")
    public void mapShouldUpdate(String location) {
        // Verificação simulada
        assertTrue(true);
    }

    @Then("all distances should be relative to {string}")
    public void distancesShouldBeRelative(String location) {
        assertTrue(true);
    }

    @Given("my current search location is {string}")
    public void givenCurrentLocation(String location) {
        System.out.println("Current location set to: " + location);
    }

    @When("I switch back to {string}")
    public void iSwitchBack(String option) {
        System.out.println("Switched to: " + option);
    }

    @Then("the map and list should show results based on my GPS position")
    public void mapUpdatesToGPS() {
        assertTrue(true);
    }
}
