package tqs.backend.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.model.Reservation;
import tqs.backend.service.ReservationService;
import tqs.backend.service.StripeClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final ReservationService reservationService;
    private final StripeClient stripeClient;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Long> request) {
        Long reservationId = request.get("reservationId");

        if (reservationId == null) {
            logger.error("Error: Missing reservationId");
            return ResponseEntity.badRequest().body(Map.of("error", "Missing reservationId"));
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            logger.debug("Found reservation: {}", (reservation != null ? "yes" : "no"));
            if (reservation != null) {
                logger.debug("Reservation details:");
                logger.debug("- Status: {}", reservation.getStatus());
                logger.debug("- Is Paid: {}", reservation.isPaid());
                logger.debug("- Estimated Cost: {}", reservation.getEstimatedCost());
            }

            if (reservation == null) {
                logger.error("Error: Reservation not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservation not found"));
            }

            if (reservation.isPaid()) {
                logger.error("Error: Reservation is already paid");
                return ResponseEntity.badRequest().body(Map.of("error", "Reservation is already paid"));
            }

            if (reservation.getEstimatedCost() == null) {
                logger.error("Error: Reservation has no estimated cost");
                return ResponseEntity.badRequest().body(Map.of("error", "Reservation cannot be paid"));
            }

            if (reservation.getStatus() != tqs.backend.model.enums.ReservationStatus.ACTIVE &&
                reservation.getStatus() != tqs.backend.model.enums.ReservationStatus.COMPLETED) {
                logger.error("Error: Invalid reservation status: {}", reservation.getStatus());
                return ResponseEntity.badRequest().body(Map.of("error", "Reservation cannot be paid"));
            }

            logger.info("Creating checkout session for reservation with cost: {}", reservation.getEstimatedCost());

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("eur")
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Charging Reservation " + reservation.getId())
                                                                    .build()
                                                    )
                                                    // Stripe expects amount in cents
                                                    .setUnitAmount(reservation.getEstimatedCost().multiply(BigDecimal.valueOf(100)).longValueExact())
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    // Replace with your actual success and cancel URLs
                    .setSuccessUrl("http://localhost:80/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:80/cancel")
                    .putMetadata("reservationId", String.valueOf(reservation.getId())) // Store reservation ID in metadata
                    .build();

            Session session = stripeClient.createCheckoutSession(params);
            if (session == null) {
                logger.error("Error: Failed to create Stripe session");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create checkout session"));
            }

            Map<String, String> responseData = new HashMap<>();
            responseData.put("sessionId", session.getId());

            return ResponseEntity.ok(responseData);

        } catch (StripeException e) {
            logger.error("Error creating checkout session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating checkout session: " + e.getMessage().split(";")[0]));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify-session")
    public ResponseEntity<?> verifyCheckoutSession(@RequestParam("session_id") String sessionId) {
        try {
            Session session = stripeClient.retrieveCheckoutSession(sessionId);

            // Check if the payment was successful
            if (!"paid".equals(session.getPaymentStatus())) {
                 return ResponseEntity.badRequest().body(Map.of("error", "Payment not completed"));
            }

            // Retrieve the reservation ID from metadata
            String reservationIdString = session.getMetadata().get("reservationId");
            if (reservationIdString == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reservation ID not found in session metadata"));
            }
            Long reservationId = Long.parseLong(reservationIdString);

            // Update the reservation status to paid
            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservation not found"));
            }

            // Prevent double payment/update if already paid
            if (reservation.isPaid()) {
                 return ResponseEntity.ok(Map.of("message", "Reservation already marked as paid"));
            }

            // reservation.getChargingSession().setPaid(true);
            reservation.setPaid(true);
            reservationService.saveReservation(reservation); // Assuming a save method exists or use update method

            return ResponseEntity.ok(Map.of("message", "Payment successfully verified and reservation updated"));

        } catch (StripeException e) {
            logger.error("Error verifying checkout session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error verifying checkout session: " + e.getMessage().split(";")[0]));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid reservation ID in session metadata"));
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
} 