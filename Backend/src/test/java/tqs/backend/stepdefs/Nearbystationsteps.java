package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;


public class Nearbystationsteps {

    @Given("the user has opened the app")
    public void user_has_opened_the_app() {
        System.out.println("App opened.");
    }

    @Given("location permission has been granted")
    public void location_permission_granted() {
        System.out.println("Location access granted.");
    }

    @When("the default location is used")
    public void default_location_used() {
        System.out.println("Using current GPS location.");
    }

    @Then("the map displays chargers as pins with availability count")
    public void map_displays_chargers() {
        System.out.println("Charger pins shown on map.");
    }

    @Then("the list displays stations sorted by distance")
    public void list_displays_sorted_stations() {
        System.out.println("Station list sorted.");
    }
}
