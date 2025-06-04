package tqs.backend.stepdefs;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.model.*;
import tqs.backend.model.enums.*;
import tqs.backend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ViewUpcomingReservationsStepDefs {

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
    private Client currentClient;
    private Station currentStation;
    private Charger currentCharger;

    @Before
    public void setup() {
        clientRepository.deleteAll();
    }

    @Given("a client is registered")
    public void a_client_is_registered() {
        currentClient = Client.builder()
                .name("Test Client")
                .email("client" + System.currentTimeMillis() + "@example.com")
                .passwordHash("hashedPassword")
                .batteryCapacityKwh(50.0)
                .fullRangeKm(300.0)
                .build();
        clientRepository.save(currentClient);
    }

    @And("a station with name {string} and address {string} and city {string} exists")
    public void a_station_with_name_address_and_city_exists(String name, String address, String city) {
        double uniqueLat = 40.0 + (name.hashCode() % 1000) / 10000.0;
        double uniqueLon = -8.0 + (name.hashCode() % 1000) / 10000.0;

        currentStation = Station.builder()
                .name(name)
                .address(address)
                .city(city)
                .latitude(uniqueLat)
                .longitude(uniqueLon)
                .build();
        stationRepository.save(currentStation);
    }

    @And("a charger with type {string} and status {string} at station {string} exists")
    public void a_charger_with_type_and_status_at_station_exists(String type, String status, String stationName) {
        Station station = stationRepository.findByName(stationName)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        currentCharger = Charger.builder()
                .station(station)
                .chargerType(ChargerType.valueOf(type))
                .status(ChargerStatus.valueOf(status))
                .pricePerKwh(BigDecimal.valueOf(0.30))
                .build();
        chargerRepository.save(currentCharger);
    }

    @And("the client has a reservation at that charger with status {string}")
    public void the_client_has_a_reservation_at_that_charger_with_status(String status) {
        Reservation reservation = Reservation.builder()
                .user(currentClient)
                .charger(currentCharger)
                .startTime(LocalDateTime.now().minusHours(1))
                .estimatedEndTime(LocalDateTime.now().minusHours(2))
                .batteryLevelStart(20.0)
                .estimatedKwh(30.0)
                .estimatedCost(new BigDecimal("15.00"))
                .status(ReservationStatus.valueOf(status))
                .build();
        reservationRepository.save(reservation);
    }

    @When("the client requests to view their reservations")
    public void the_client_requests_to_view_their_reservations() {
        latestResponse = given()
                .port(port)
                .when()
                .get("/api/reservations/client/" + currentClient.getId());
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int statusCode) {
        assertThat(latestResponse.getStatusCode(), is(statusCode));
    }

    @And("the response should contain exactly {int} reservation(s)")
    public void the_response_should_contain_exactly_n_reservations(int count) {
        latestResponse.then().body("$", hasSize(count));
    }

    @And("one reservation should have status {string}")
    public void one_reservation_should_have_status(String status) {
        latestResponse.then().body("status", hasItem(status));
    }

    @And("the {string} reservation should include station name {string} and address {string} and city {string}")
    public void the_reservation_should_include_station_name_address_and_city(
            String status, String stationName, String address, String city) {

        latestResponse.then()
                .body("find { it.status == '" + status + "' }.charger.station.name", equalTo(stationName))
                .body("find { it.status == '" + status + "' }.charger.station.address", equalTo(address))
                .body("find { it.status == '" + status + "' }.charger.station.city", equalTo(city));
    }
}
