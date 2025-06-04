package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import tqs.backend.model.*;
import tqs.backend.repository.*;

import java.util.HashMap;
import java.util.Map;

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

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(signUp)
                .post("/api/clients/signup");

        assertThat(latestResponse.getStatusCode(), is(HttpStatus.CREATED.value()));

        Client client = clientRepository.findByEmail(email).orElseThrow();
        clientId = client.getId();
    }

    @And("a station {string} exists with an available charger")
    public void create_station_and_charger(String stationName) {
        Map<String, Object> station = new HashMap<>();
        station.put("name", stationName);
        station.put("latitude", 40.0);
        station.put("longitude", -8.0);

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(station)
                .post("/api/stations");

        assertThat(latestResponse.getStatusCode(), is(HttpStatus.CREATED.value()));

        Station createdStation = stationRepository
                .findByLatitudeAndLongitude(40.0, -8.0)
                .orElseThrow();
        stationId = createdStation.getId();

        Map<String, Object> charger = new HashMap<>();
        charger.put("stationId", stationId);
        charger.put("type", "DC_FAST");

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(charger)
                .post("/api/chargers");

        assertThat(latestResponse.getStatusCode(), is(HttpStatus.CREATED.value()));
    }

    @When("the client reserves a charger at {string}")
    public void make_reservation(String dateTime) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("clientId", clientId);
        reservation.put("stationId", stationId);
        reservation.put("reservationTime", dateTime);

        latestResponse = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(reservation)
                .post("/api/reservations");
    }

    @Then("the reservation is created successfully with status {string}")
    public void verify_reservation(String expectedStatus) {
        assertThat(latestResponse.getStatusCode(), is(HttpStatus.CREATED.value()));
        String status = latestResponse.jsonPath().getString("status");
        assertThat(status, equalTo(expectedStatus));
    }
}
