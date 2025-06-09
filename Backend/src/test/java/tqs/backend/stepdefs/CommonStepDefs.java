package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import io.restassured.RestAssured;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import tqs.backend.BackendApplication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = tqs.backend.stepdefs.CucumberSpringConfiguration.class)
public class CommonStepDefs {

    @LocalServerPort
    private int port;

    @Given("the system is running")
    public void theSystemIsRunning() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
