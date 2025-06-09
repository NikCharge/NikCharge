package tqs.backend.stepdefs;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.model.Reservation;
import tqs.backend.repository.ReservationRepository;
import tqs.backend.service.StripeClient;
import com.stripe.model.checkout.Session;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentStepDefs {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockBean
    private StripeClient stripeClient;

    public static Reservation activeReservation;
    public static Reservation completedReservation;

    @BeforeEach
    public void setup() throws StripeException {
        // Mock Stripe client responses
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("cs_test_123");
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        
        // Mock session creation to return the mock session with dynamic metadata
        when(stripeClient.createCheckoutSession(any(SessionCreateParams.class))).thenAnswer(invocation -> {
            SessionCreateParams params = invocation.getArgument(0);
            System.out.println("Creating mock session with params: " + params);
            Map<String, String> metadata = new HashMap<>();
            String reservationId = params.getMetadata().get("reservationId");
            System.out.println("Reservation ID from params: " + reservationId);
            metadata.put("reservationId", reservationId);
            when(mockSession.getMetadata()).thenReturn(metadata);
            return mockSession;
        });
        
        when(stripeClient.retrieveCheckoutSession(any())).thenReturn(mockSession);

        // Ensure reservations are properly persisted with all required fields
        if (activeReservation != null) {
            activeReservation.setStatus(tqs.backend.model.enums.ReservationStatus.ACTIVE);
            activeReservation.setPaid(false);
            activeReservation.setEstimatedCost(new java.math.BigDecimal("10.00"));
            activeReservation = reservationRepository.saveAndFlush(activeReservation);
            System.out.println("Active reservation persisted with ID: " + activeReservation.getId());
            System.out.println("Active reservation status: " + activeReservation.getStatus());
            System.out.println("Active reservation is paid: " + activeReservation.isPaid());
            System.out.println("Active reservation cost: " + activeReservation.getEstimatedCost());
        }
        if (completedReservation != null) {
            completedReservation.setStatus(tqs.backend.model.enums.ReservationStatus.COMPLETED);
            completedReservation.setPaid(false);
            completedReservation.setEstimatedCost(new java.math.BigDecimal("10.00"));
            completedReservation = reservationRepository.saveAndFlush(completedReservation);
            System.out.println("Completed reservation persisted with ID: " + completedReservation.getId());
            System.out.println("Completed reservation status: " + completedReservation.getStatus());
            System.out.println("Completed reservation is paid: " + completedReservation.isPaid());
            System.out.println("Completed reservation cost: " + completedReservation.getEstimatedCost());
        }
    }

    @When("I attempt to initiate payment for reservation {string}")
    public void iAttemptToInitiatePaymentForReservation(String reservationId) {
        Response response = RestAssured
                .given()
                .port(port)
                .contentType("application/json")
                .body("{\"reservationId\": " + reservationId + "}")
                .post("/api/payment/create-checkout-session");

        CommonResponseStepDefs.latestResponse = response;
    }

    @When("I attempt to initiate payment for the active reservation")
    public void iAttemptToInitiatePaymentForActiveReservation() {
        Long id = activeReservation.getId();
        System.out.println("Initiating payment for active reservation ID: " + id);
        System.out.println("Active reservation details before payment:");
        System.out.println("- Status: " + activeReservation.getStatus());
        System.out.println("- Is Paid: " + activeReservation.isPaid());
        System.out.println("- Estimated Cost: " + activeReservation.getEstimatedCost());
        
        Response response = RestAssured
                .given()
                .port(port)
                .contentType("application/json")
                .body("{\"reservationId\": " + id + "}")
                .post("/api/payment/create-checkout-session");

        System.out.println("Payment response status: " + response.getStatusCode());
        System.out.println("Payment response body: " + response.getBody().asString());

        CommonResponseStepDefs.latestResponse = response;
    }

    @When("I attempt to initiate payment for the completed reservation")
    public void iAttemptToInitiatePaymentForCompletedReservation() {
        Long id = completedReservation.getId();
        System.out.println("Initiating payment for completed reservation ID: " + id);
        System.out.println("Completed reservation details before payment:");
        System.out.println("- Status: " + completedReservation.getStatus());
        System.out.println("- Is Paid: " + completedReservation.isPaid());
        System.out.println("- Estimated Cost: " + completedReservation.getEstimatedCost());
        
        Response response = RestAssured
                .given()
                .port(port)
                .contentType("application/json")
                .body("{\"reservationId\": " + id + "}")
                .post("/api/payment/create-checkout-session");

        System.out.println("Payment response status: " + response.getStatusCode());
        System.out.println("Payment response body: " + response.getBody().asString());

        CommonResponseStepDefs.latestResponse = response;
    }

    @And("the active reservation is already paid")
    public void theActiveReservationIsAlreadyPaid() {
        activeReservation.setPaid(true);
        reservationRepository.saveAndFlush(activeReservation);
    }

    @Given("I have successfully paid for the completed reservation")
    public void iHaveSuccessfullyPaidForTheCompletedReservation() {
        completedReservation.setPaid(true);
        reservationRepository.saveAndFlush(completedReservation);
    }

    @When("I fetch the completed reservation")
    public void iFetchTheCompletedReservation() {
        CommonResponseStepDefs.latestResponse = RestAssured
                .given()
                .port(port)
                .get("/api/reservations/" + completedReservation.getId());
    }

    @Then("I should see {string} status for the completed reservation")
    public void iShouldSeePaidStatusForTheCompletedReservation(String expectedStatus) {
        assertThat(completedReservation.isPaid(), is(expectedStatus.equalsIgnoreCase("Paid")));
    }
}
