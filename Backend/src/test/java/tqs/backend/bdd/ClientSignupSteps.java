package tqs.backend.bdd;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ClientSignupSteps {

    private Map<String, Object> clientData = new HashMap<>();
    private Response response;

    @Given("a client with name {string}, email {string}, password {string}, battery {int}, and range {int}")
    public void a_client_with_data(String name, String email, String password, int battery, int range) {
        clientData.put("name", name);
        clientData.put("email", email);
        clientData.put("password", password);
        clientData.put("batteryCapacityKwh", battery);
        clientData.put("fullRangeKm", range);
    }

    @When("the client signs up")
    public void the_client_signs_up() {
        response = RestAssured.given()
                .contentType("application/json")
                .body(clientData)
                .post("http://localhost:8080/api/clients/signup");
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int status) {
        assertThat(response.getStatusCode(), is(status));
    }

    @Then("the response should contain email {string}")
    public void the_response_should_contain_email(String email) {
        assertThat(response.jsonPath().getString("email"), is(email));
    }
}