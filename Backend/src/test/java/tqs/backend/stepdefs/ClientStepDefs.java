package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.backend.model.Client;
import tqs.backend.util.CommonReservationHelper;

public class ClientStepDefs {

    @Autowired
    private CommonReservationHelper helper;

    public static Client currentClient;

    @Given("a client is registered with email {string} and password {string}")
    public void register_client_with_credentials(String email, String password) {
        currentClient = helper.createClient("Test Client", email, password);
    }
}
