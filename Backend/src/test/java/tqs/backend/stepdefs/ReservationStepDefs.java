package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.model.*;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.repository.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReservationStepDefs {

    @LocalServerPort
    private int port;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Response latestResponse;
    private Long clientId;
    private Long stationId;

    @Given("a client is registered with email {string} and password {string}")
    public void register_client(String email, String password) {
        Map<String, Object> signUp = new HashMap<>();
        signUp.put("email", email);
        signUp.put("password", password);
        signUp.put("name", "Test Client");
        signUp.put("batteryCapacityKwh", 50.0);
        signUp.put("fullRangeKm", 300.0);

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(signUp)
                .post("/api/clients/signup");

        assertThat(latestResponse.getStatusCode(), anyOf(is(200), is(201)));

        Client client = clientRepository.findByEmail(email).orElseThrow();
        clientId = client.getId();
    }

    @And("a station {string} exists with an available charger")
    public void create_station_and_charger(String stationName) {
        Map<String, Object> station = new HashMap<>();
        station.put("name", stationName);
        station.put("address", "Main Ave");
        station.put("city", "Testville");
        station.put("latitude", 40.0);
        station.put("longitude", -8.0);

        RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(station)
                .post("/api/stations");

        stationId = stationRepository.findByLatitudeAndLongitude(40.0, -8.0).orElseThrow().getId();

        Map<String, Object> charger = new HashMap<>();
        charger.put("stationId", stationId);
        charger.put("type", "DC_FAST");
        charger.put("status", "AVAILABLE");

        RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(charger)
                .post("/api/chargers");
    }

    @And("a station {string} exists without available chargers")
    public void create_station_without_available_chargers(String stationName) {
        Map<String, Object> station = new HashMap<>();
        station.put("name", stationName);
        station.put("address", "No Charger Blvd");
        station.put("city", "Drytown");
        station.put("latitude", 41.0);
        station.put("longitude", -9.0);

        RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(station)
                .post("/api/stations");

        stationId = stationRepository.findByLatitudeAndLongitude(41.0, -9.0).orElseThrow().getId();

        Map<String, Object> charger = new HashMap<>();
        charger.put("stationId", stationId);
        charger.put("type", "DC_FAST");
        charger.put("status", "IN_USE");

        RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(charger)
                .post("/api/chargers");
    }

    @When("the client reserves a charger at {string}")
    public void make_reservation(String dateTime) {
        Optional<Charger> optionalCharger = chargerRepository.findByStationId(stationId).stream()
                .filter(c -> c.getStatus() == ChargerStatus.AVAILABLE)
                .findFirst();

        if (optionalCharger.isEmpty()) {
            latestResponse = RestAssured.given()
                    .port(port)
                    .contentType("application/json")
                    .body(new HashMap<>())
                    .post("/api/reservations");
            return;
        }

        Charger charger = optionalCharger.get();

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("clientId", clientId);
        reservation.put("chargerId", charger.getId());
        reservation.put("startTime", dateTime);
        reservation.put("estimatedEndTime", "2025-06-05T11:00:00");
        reservation.put("batteryLevelStart", 20.0);
        reservation.put("estimatedKwh", 25.0);
        reservation.put("estimatedCost", 5.99);

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(reservation)
                .post("/api/reservations");
    }

    @And("a reservation already exists at {string}")
    public void existing_reservation(String startTime) {
        Charger charger = chargerRepository.findByStationId(stationId).stream()
                .filter(c -> c.getStatus() == ChargerStatus.AVAILABLE)
                .findFirst()
                .orElseThrow();

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("clientId", clientId);
        reservation.put("chargerId", charger.getId());
        reservation.put("startTime", startTime);
        reservation.put("estimatedEndTime", "2025-06-07T11:00:00");
        reservation.put("batteryLevelStart", 30.0);
        reservation.put("estimatedKwh", 20.0);
        reservation.put("estimatedCost", 4.99);

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(reservation)
                .post("/api/reservations");

        assertThat(latestResponse.getStatusCode(), anyOf(is(200), is(201)));
    }

    @When("the client tries to reserve with a non-existent charger at {string}")
    public void reserve_with_invalid_charger(String dateTime) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("clientId", clientId);
        reservation.put("chargerId", -999L);
        reservation.put("startTime", dateTime);
        reservation.put("estimatedEndTime", "2025-06-08T11:00:00");
        reservation.put("batteryLevelStart", 10.0);
        reservation.put("estimatedKwh", 10.0);
        reservation.put("estimatedCost", 3.0);

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(reservation)
                .post("/api/reservations");
    }

    @When("the client submits an incomplete reservation")
    public void submit_incomplete_reservation() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("clientId", clientId); // Missing required fields

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(reservation)
                .post("/api/reservations");
    }

    @Then("the reservation is created successfully with status {string}")
    public void verify_reservation(String expectedStatus) {
        assertThat(latestResponse.getStatusCode(), anyOf(is(200), is(201)));
        String status = latestResponse.jsonPath().getString("status");
        assertThat(status, equalTo(expectedStatus));
    }

    @Then("the reservation creation should fail with status code {int}")
    public void reservation_should_fail(int statusCode) {
        assertThat(latestResponse.getStatusCode(), is(statusCode));
    }
}
