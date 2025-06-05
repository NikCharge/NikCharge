package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import tqs.backend.BackendApplication;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import io.cucumber.java.Before;
import tqs.backend.repository.ClientRepository;

import static org.hamcrest.Matchers.*;

// @CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = BackendApplication.class)
public class UserRegistrationStepDefs {

        

        @LocalServerPort
        private int port;

        private Response response;
        private Map<String, String> registrationData;

        @Autowired
        private ClientRepository clientRepository;

        @Before
        public void setup() {
                clientRepository.deleteAll();
        }

        @Given("a user with email {string} exists in the system")
        public void aUserWithEmailExistsInTheSystem(String email) {
                // Create a test user in the system
                RestAssured.given()
                                .contentType("application/json")
                                .body("{\"email\": \"" + email
                                                + "\", \"password\": \"Password123!\", \"name\": \"Test User\", \"batteryCapacityKwh\": 50.0, \"fullRangeKm\": 300.0}")
                                .when()
                                .post("/api/clients/signup");
        }

        @When("I submit registration with the following details")
        public void iSubmitRegistrationWithTheFollowingDetails(DataTable dataTable) {
                registrationData = dataTable.asMaps().get(0);

                response = RestAssured.given()
                                .contentType("application/json")
                                .body("{\"email\": \"" + registrationData.get("email") + "\", " +
                                                "\"password\": \"" + registrationData.get("password") + "\", " +
                                                "\"name\": \"" + registrationData.get("name") + "\", " +
                                                "\"batteryCapacityKwh\": 50.0, " +
                                                "\"fullRangeKm\": 300.0}")
                                .when()
                                .post("/api/clients/signup");
        }

        @Then("the registration should be successful")
        public void theRegistrationShouldBeSuccessful() {
                response.then()
                                .statusCode(200);
        }

        @And("I should receive a confirmation message")
        public void iShouldReceiveAConfirmationMessage() {
                response.then()
                                .body("email", equalTo(registrationData.get("email")))
                                .body("name", equalTo(registrationData.get("name")));
        }

        @Then("the registration should fail")
        public void theRegistrationShouldFail() {
                response.then()
                                .statusCode(anyOf(is(400), is(409)));
        }

        @And("I should receive an error message about invalid email format")
        public void iShouldReceiveAnErrorMessageAboutInvalidEmailFormat() {
                response.then()
                                .body(containsString("Invalid email format"));
        }

        @And("I should receive an error message about password requirements")
        public void iShouldReceiveAnErrorMessageAboutPasswordRequirements() {
                response.then()
                                .body(containsString("Password must be at least 8 characters"));
        }

        @And("I should receive an error message about email already in use")
        public void iShouldReceiveAnErrorMessageAboutEmailAlreadyInUse() {
                response.then()
                                .statusCode(409)
                                .body(containsString("Email already exists"));
        }
}