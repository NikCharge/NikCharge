package tqs.backend.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.dto.ReservationRequest;
import tqs.backend.dto.ReservationResponse;
import tqs.backend.model.Reservation;
import tqs.backend.service.ReservationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    private static final String ERROR_KEY = "error";

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getReservationsByClientId(@PathVariable Long clientId) {
        try {
            List<ReservationResponse> reservations = reservationService.getReservationsByClientId(clientId);
            return new ResponseEntity<>(reservations, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error fetching reservations for client {}: {}", clientId, e.getMessage());
            return handleRuntimeException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        try {
            Reservation reservation = reservationService.createReservation(request);
            return new ResponseEntity<>(reservation, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Error creating reservation: {}", e.getMessage());
            return handleRuntimeException(e);
        }
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        try {
            Reservation reservation = reservationService.cancelReservation(reservationId);
            return new ResponseEntity<>(reservation, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error cancelling reservation: {}", e.getMessage());
            if (e.getMessage().equals("Reservation not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
            } else if (e.getMessage().equals("Invalid reservation status: only active reservations can be cancelled")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
            }
            return handleRuntimeException(e);
        }
    }

    @PutMapping("/{reservationId}/complete")
    public ResponseEntity<?> completeReservation(@PathVariable Long reservationId) {
        try {
            Reservation reservation = reservationService.completeReservation(reservationId);
            return new ResponseEntity<>(reservation, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error completing reservation: {}", e.getMessage());
            if (e.getMessage().equals("Reservation not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
            } else if (e.getMessage().startsWith("Invalid reservation status:")) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
            }
            return handleRuntimeException(e);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        logger.error("RuntimeException caught by handler: {}", ex.getMessage());
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put(ERROR_KEY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }
}
