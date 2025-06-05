package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.model.Reservation;
import tqs.backend.repository.ReservationRepository;
import tqs.backend.util.CommonReservationHelper;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CancelReservationStepDefs {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CommonReservationHelper helper;

    @When("the client cancels the reservation")
    public void the_client_cancels_the_reservation() {
        Reservation reservation = ReservationStepDefs.currentReservation;
        CommonResponseStepDefs.latestResponse = RestAssured.given()
                .port(port)
                .delete("/api/reservations/" + reservation.getId());
    }

    @When("the client attempts to cancel a non-existent reservation")
    public void the_client_attempts_to_cancel_non_existent_reservation() {
        CommonResponseStepDefs.latestResponse = RestAssured.given()
                .port(port)
                .delete("/api/reservations/-999");
    }

    @When("the client attempts to cancel the reservation again")
    public void the_client_attempts_to_cancel_the_reservation_again() {
        Reservation reservation = ReservationStepDefs.currentReservation;
        CommonResponseStepDefs.latestResponse = RestAssured.given()
                .port(port)
                .delete("/api/reservations/" + reservation.getId());
    }

    @Then("the reservation should be deleted from the database")
    public void the_reservation_should_be_deleted() {
        Reservation reservation = ReservationStepDefs.currentReservation;
        Optional<Reservation> deleted = reservationRepository.findById(reservation.getId());
        assertThat(deleted.isPresent(), is(false));
    }

    @Then("the cancellation should fail with status code {int}")
    public void cancellation_should_fail(int statusCode) {
        assertThat(CommonResponseStepDefs.latestResponse.getStatusCode(), is(statusCode));
    }

    @Then("the response should contain an error message about invalid status")
    public void the_response_should_contain_error_message() {
        String error = CommonResponseStepDefs.latestResponse.getBody().asString();
        assertThat(error.toLowerCase(), containsString("invalid"));
    }
}
