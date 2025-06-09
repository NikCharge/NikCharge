package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.model.*;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.model.enums.ReservationStatus;
import tqs.backend.repository.*;
import tqs.backend.util.CommonReservationHelper;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = tqs.backend.stepdefs.CucumberSpringConfiguration.class)
public class MakeReservationStepDefs {

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

    @Autowired
    private CommonReservationHelper helper;

    private Response latestResponse;

    @And("a station {string} exists without available chargers")
    public void create_station_without_available_chargers(String stationName) {
        Station station = helper.createStation(stationName, "No Charger Blvd", "Drytown");
        StationStepDefs.currentStation = station;
        helper.createCharger(station, ChargerType.DC_FAST, ChargerStatus.IN_USE);
    }

    @When("the client reserves a charger at {string}")
    public void make_reservation(String dateTime) {
        Optional<Charger> optionalCharger = chargerRepository.findByStationId(StationStepDefs.currentStation.getId()).stream()
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
        reservation.put("clientId", ClientStepDefs.currentClient.getId());
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
        Charger charger = chargerRepository.findByStationId(StationStepDefs.currentStation.getId()).stream()
                .filter(c -> c.getStatus() == ChargerStatus.AVAILABLE)
                .findFirst()
                .orElseThrow();

        helper.createReservation(
                ClientStepDefs.currentClient,
                charger,
                ReservationStatus.ACTIVE,
                LocalDateTime.parse(startTime),
                LocalDateTime.parse("2025-06-07T11:00:00")
        );
    }

    @When("the client tries to reserve with a non-existent charger at {string}")
    public void reserve_with_invalid_charger(String dateTime) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("clientId", ClientStepDefs.currentClient.getId());
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
        reservation.put("clientId", ClientStepDefs.currentClient.getId()); // Missing other fields

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
