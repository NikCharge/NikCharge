package tqs.backend.stepdefs;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.util.CommonReservationHelper;
import static org.hamcrest.Matchers.equalTo;

import static io.restassured.RestAssured.given;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = tqs.backend.stepdefs.CucumberSpringConfiguration.class)
public class ViewUpcomingReservationsStepDefs {

    @LocalServerPort
    private int port;

    @Autowired
    private CommonReservationHelper helper;

    @Before
    public void setup() {
        helper.clearAllClients();
    }

    @And("a station with name {string} and address {string} and city {string} exists")
    public void a_station_with_name_address_and_city_exists(String name, String address, String city) {
        StationStepDefs.currentStation = helper.createStation(name, address, city);
    }

    @And("a charger with type {string} and status {string} at station {string} exists")
    public void a_charger_with_type_and_status_at_station_exists(String type, String status, String stationName) {
        StationStepDefs.currentCharger = helper.createCharger(
                StationStepDefs.currentStation,
                ChargerType.valueOf(type),
                ChargerStatus.valueOf(status)
        );
    }

    @When("the client requests to view their reservations")
    public void the_client_requests_to_view_their_reservations() {
        CommonResponseStepDefs.latestResponse = given()
                .port(port)
                .when()
                .get("/api/reservations/client/" + ClientStepDefs.currentClient.getId());
    }

    @And("the {string} reservation should include station name {string} and address {string} and city {string}")
    public void the_reservation_should_include_station_name_address_and_city(
            String status, String stationName, String address, String city) {

        CommonResponseStepDefs.latestResponse.then()
                .body("find { it.status == '" + status + "' }.charger.station.name", equalTo(stationName))
                .body("find { it.status == '" + status + "' }.charger.station.address", equalTo(address))
                .body("find { it.status == '" + status + "' }.charger.station.city", equalTo(city));
    }
}
