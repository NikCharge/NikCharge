package tqs.backend.stepdefs;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static org.hamcrest.Matchers.*;

public class UserProfileStepDefs {

    private Response response;
    private Map<String, String> updateData;

    @When("I update the profile for {string} with the following data")
    public void iUpdateTheProfileForWithTheFollowingData(String currentEmail, DataTable dataTable) {
        updateData = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .contentType("application/json")
                .body(Map.of(
                        "name", updateData.get("name"),
                        "email", updateData.get("email"),
                        "batteryCapacityKwh", Double.valueOf(updateData.get("batteryCapacityKwh")),
                        "fullRangeKm", Double.valueOf(updateData.get("fullRangeKm"))
                ))
                .when()
                .put("/api/clients/" + currentEmail);
    }

    @Then("the profile should be updated successfully")
    public void theProfileShouldBeUpdatedSuccessfully() {
        response.then().statusCode(200);
    }

    @And("the response should contain the updated profile data")
    public void theResponseShouldContainTheUpdatedProfileData() {
        response.then()
                .body("email", equalTo(updateData.get("email")))
                .body("name", equalTo(updateData.get("name")))
                .body("batteryCapacityKwh", equalTo(Float.parseFloat(updateData.get("batteryCapacityKwh"))))
                .body("fullRangeKm", equalTo(Float.parseFloat(updateData.get("fullRangeKm"))));
    }

    @Then("the update should fail with status {int}")
    public void theUpdateShouldFailWithStatus(int status) {
        response.then().statusCode(status);
    }

    @And("I should receive an error message about user not found")
    public void iShouldReceiveAnErrorMessageAboutUserNotFound() {
        response.then().body("error", equalTo("Client not found"));
    }
}
