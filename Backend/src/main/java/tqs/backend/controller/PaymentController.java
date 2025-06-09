package tqs.backend.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    private final ReservationService reservationService;
    private final StripeClient stripeClient;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Long> request) {
        Long reservationId = request.get("reservationId");
        System.out.println("Attempting to create checkout session for reservation ID: " + reservationId);

        if (reservationId == null) {
            System.out.println("Error: Missing reservationId");
            return ResponseEntity.badRequest().body(Map.of("error", "Missing reservationId"));
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            System.out.println("Found reservation: " + (reservation != null ? "yes" : "no"));
            if (reservation != null) {
                System.out.println("Reservation details:");
                System.out.println("- Status: " + reservation.getStatus());
                System.out.println("- Is Paid: " + reservation.isPaid());
                System.out.println("- Estimated Cost: " + reservation.getEstimatedCost());
            }

            if (reservation == null) {
                System.out.println("Error: Reservation not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservation not found"));
            }

            if (reservation.isPaid()) {
                System.out.println("Error: Reservation is already paid");
                return ResponseEntity.badRequest().body(Map.of("error", "Reservation is already paid"));
            }

            if (reservation.getEstimatedCost() == null) {
                System.out.println("Error: Reservation has no estimated cost");
                return ResponseEntity.badRequest().body(Map.of("error", "Reservation cannot be paid"));
            }

            if (reservation.getStatus() != tqs.backend.model.enums.ReservationStatus.ACTIVE &&
                reservation.getStatus() != tqs.backend.model.enums.ReservationStatus.COMPLETED) {
                System.out.println("Error: Invalid reservation status: " + reservation.getStatus());
                return ResponseEntity.badRequest().body(Map.of("error", "Reservation cannot be paid"));
            }

            System.out.println("Creating checkout session for reservation with cost: " + reservation.getEstimatedCost());

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
                System.out.println("Error: Failed to create Stripe session");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create payment session"));
            }

            Map<String, String> responseData = new HashMap<>();
            responseData.put("sessionId", session.getId());

            return ResponseEntity.ok(responseData);

        } catch (StripeException e) {
            // Log the error for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating checkout session: " + e.getMessage()));
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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error verifying checkout session: " + e.getMessage()));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid reservation ID in session metadata"));
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
} 