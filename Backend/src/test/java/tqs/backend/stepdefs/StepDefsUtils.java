package tqs.backend.stepdefs;

import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StepDefsUtils {
    
    public static void verifyResponseStatus(ResultActions resultActions, int expectedStatusCode) throws Exception {
        resultActions.andExpect(status().is(expectedStatusCode));
    }
} 