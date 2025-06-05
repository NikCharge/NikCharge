package tqs.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class StripeClient {

    @Value("${stripe.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public Session createCheckoutSession(SessionCreateParams params) throws StripeException {
        return Session.create(params);
    }

    public Session retrieveCheckoutSession(String id) throws StripeException {
        return Session.retrieve(id);
    }
} 