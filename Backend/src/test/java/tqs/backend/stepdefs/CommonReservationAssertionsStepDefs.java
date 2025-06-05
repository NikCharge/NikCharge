package tqs.backend.stepdefs;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import static org.hamcrest.Matchers.*;

public class CommonReservationAssertionsStepDefs {

    @Then("the response should contain exactly {int} reservation\\(s)")
    public void the_response_should_contain_exactly_reservations(int count) {
        CommonResponseStepDefs.latestResponse.then().statusCode(200);
        CommonResponseStepDefs.latestResponse.then().body("$", hasSize(count));
    }

    @Then("the response should contain exactly {int} reservation")
    public void the_response_should_contain_exactly_reservation(int count) {
        the_response_should_contain_exactly_reservations(count);
    }

    @And("one reservation should have status {string}")
    public void one_reservation_should_have_status(String status) {
        CommonResponseStepDefs.latestResponse.then().body("status", hasItem(status));
    }
}
