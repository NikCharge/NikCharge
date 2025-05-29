package com.nikcharge.backend.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.nikcharge.backend.cucumber.steps",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cucumber-pretty.html",
        "json:target/cucumber-reports/cucumber.json",
        "com.xpandit.xray.cucumber.XrayCucumberPlugin"
    },
    tags = "@SCRUM-11 "
)
public class CucumberRunner {
} 