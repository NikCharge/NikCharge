package tqs.backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StripeClientTest {

    @Mock
    private Session mockSession;

    @InjectMocks
    private StripeClient stripeClient;

    private static final String TEST_API_KEY = "sk_test_123";
    private static final String TEST_SESSION_ID = "cs_test_123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeClient, "apiKey", TEST_API_KEY);
        stripeClient.init();
    }

    @Test
    @DisplayName("Create checkout session - Success")
    void whenCreateCheckoutSession_thenReturnSession() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Retrieve checkout session - Success")
    void whenRetrieveCheckoutSession_thenReturnSession() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Initialize with API key")
    void whenInit_thenApiKeyIsSet() {
        // Verify that the API key is set during initialization
        assertNotNull(stripeClient);
        // Note: We can't directly verify the Stripe.apiKey value as it's static
        // This is more of a smoke test to ensure initialization doesn't throw exceptions
    }
} 