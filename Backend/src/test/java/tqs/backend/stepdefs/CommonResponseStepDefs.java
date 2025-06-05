package tqs.backend.stepdefs;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommonResponseStepDefs {

    public static Response latestResponse;

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int code) {
        assertThat(latestResponse.getStatusCode(), is(code));
    }
}
