package tqs.backend.stepdefs;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tqs.backend.BackendApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = BackendApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}