package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

import tqs.backend.model.Station;
import tqs.backend.service.StationService;
import tqs.backend.repository.StationRepository;
import tqs.backend.util.GeoUtils;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class Nearbystationsteps {

    private StationService stationService;
    private List<Station> allStations;
    private List<Station> nearbyStations;

    private final double userLat = 40.64;
    private final double userLng = -8.65;

    @Given("the user has opened the app")
    public void user_has_opened_the_app() {
        allStations = new ArrayList<>();
        StationRepository repo = mock(StationRepository.class);

        // Create stations (near and far)
        Station s1 = new Station(1L, "Aveiro Central", "Address A", "Aveiro", 40.64, -8.65, null); // ~0 km
        Station s2 = new Station(2L, "Oporto Station", "Address B", "Porto", 41.15, -8.61, null);  // ~60 km

        allStations.add(s1);
        allStations.add(s2);

        when(repo.findAll()).thenReturn(allStations);
        stationService = new StationService(repo);

        System.out.println("App opened with mock stations.");
    }

    @Given("location permission has been granted")
    public void location_permission_granted() {
        System.out.println("Location access granted.");
    }

    @When("the default location is used")
    public void default_location_used() {
        nearbyStations = stationService.findNearbyStations(userLat, userLng, 10); // radius 10km
    }

    @Then("the map displays chargers as pins with availability count")
    public void map_displays_chargers() {
        assertNotNull(nearbyStations);
        assertTrue(nearbyStations.stream().allMatch(
                s -> GeoUtils.calculateDistance(userLat, userLng, s.getLatitude(), s.getLongitude()) <= 10
        ));
        System.out.println("Charger pins shown on map for " + nearbyStations.size() + " stations.");
    }

    @Then("the list displays stations sorted by distance")
    public void list_displays_sorted_stations() {
        List<Station> sorted = stationService.sortByDistance(nearbyStations, userLat, userLng);
        for (int i = 0; i < sorted.size() - 1; i++) {
            double d1 = GeoUtils.calculateDistance(userLat, userLng, sorted.get(i).getLatitude(), sorted.get(i).getLongitude());
            double d2 = GeoUtils.calculateDistance(userLat, userLng, sorted.get(i + 1).getLatitude(), sorted.get(i + 1).getLongitude());
            assertTrue(d1 <= d2, "Stations are not sorted by distance.");
        }
        System.out.println("Stations sorted by distance.");
    }
}
