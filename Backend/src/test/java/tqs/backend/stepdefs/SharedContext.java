package tqs.backend.stepdefs;

import org.springframework.test.web.servlet.ResultActions;

public class SharedContext {
    private static ResultActions latestResultActions;

    public static void setLatestResultActions(ResultActions resultActions) {
        latestResultActions = resultActions;
    }

    public static ResultActions getLatestResultActions() {
        return latestResultActions;
    }
} 