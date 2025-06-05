package tqs.backend.stepdefs;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.util.CommonReservationHelper;
import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ViewCompletedReservationsStepDefs {

    @LocalServerPort
    private int port;

    @Autowired
    private CommonReservationHelper helper;

    @Before
    public void setup() {
        helper.clearAllClients();
    }

    @When("the client requests to view their completed reservations")
    public void the_client_requests_to_view_completed_reservations() {
        CommonResponseStepDefs.latestResponse = given()
                .port(port)
                .when()
                .get("/api/reservations/client/" + ClientStepDefs.currentClient.getId() + "?status=COMPLETED");
    }
}