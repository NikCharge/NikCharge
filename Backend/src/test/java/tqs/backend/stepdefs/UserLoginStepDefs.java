package tqs.backend.stepdefs;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static org.hamcrest.Matchers.*;

public class UserLoginStepDefs {

    private Response response;
    private Map<String, String> loginData;

    @Given("a registered user with email {string} and password {string}")
    public void aRegisteredUserWithEmailAndPassword(String email, String password) {
        RestAssured.given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Test User",
                        "email", email,
                        "password", password,
                        "batteryCapacityKwh", 50,
                        "fullRangeKm", 300))
                .when()
                .post("/api/clients/signup")
                .then()
                .statusCode(200);
    }

    @When("I submit login with the following credentials")
    public void iSubmitLoginWithTheFollowingCredentials(DataTable dataTable) {
        loginData = dataTable.asMaps().get(0);
        response = RestAssured.given()
                .contentType("application/json")
                .body(loginData)
                .when()
                .post("/api/clients/login");
    }

    @Then("the login should be successful")
    public void theLoginShouldBeSuccessful() {
        response.then().statusCode(200);
    }

    @And("I should receive an authentication token")
    public void iShouldReceiveAnAuthenticationToken() {
        response.then()
                .body("email", equalTo(loginData.get("email")))
                .body("name", notNullValue())
                .body("batteryCapacityKwh", notNullValue())
                .body("fullRangeKm", notNullValue());
    }

    @Then("the login should be forbidden")
    public void theLoginShouldBeForbidden() {
        response.then().statusCode(403);
    }

    @Then("I should receive an error message about invalid credentials")
    public void iShouldReceiveAnErrorMessageAboutInvalidCredentials() {
        response.then().body("error", equalTo("Invalid credentials"));
    }

    @Then("the login should fail")
    public void theLoginShouldFail() {
        response.then().statusCode(400);
    }

    @Then("I should receive an error message about missing fields")
    public void iShouldReceiveAnErrorMessageAboutMissingFields() {
        response.then().body("error.password", notNullValue());
    }

    @And("the error message should mention missing password")
    public void theErrorMessageShouldMentionMissingPassword() {
        response.then().body("error.password", notNullValue());
    }

    @And("the error message should mention invalid email format")
    public void theErrorMessageShouldMentionInvalidEmailFormat() {
        response.then().body("error.email", notNullValue());
    }
}
