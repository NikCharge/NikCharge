package tqs.backend.stepdefs;

import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.backend.model.Reservation;
import tqs.backend.model.enums.ReservationStatus;
import tqs.backend.repository.ReservationRepository;
import tqs.backend.util.CommonReservationHelper;
import java.math.BigDecimal;

import java.time.LocalDateTime;

public class ReservationStepDefs {

    @Autowired
    private CommonReservationHelper helper;

    public static Reservation currentReservation;

    @Autowired
    private ReservationRepository reservationRepository;

    @And("the client has a reservation at that charger with status {string}")
    public void the_client_has_a_reservation_at_that_charger_with_status(String status) {
        Reservation reservation = helper.createReservation(
                ClientStepDefs.currentClient,
                StationStepDefs.currentCharger,
                ReservationStatus.valueOf(status),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );

        reservation.setEstimatedCost(BigDecimal.valueOf(10)); // Ensure cost is set
        reservation = reservationRepository.saveAndFlush(reservation); // Force write to DB

        if ("ACTIVE".equals(status)) {
            PaymentStepDefs.activeReservation = reservation;
        } else if ("COMPLETED".equals(status)) {
            PaymentStepDefs.completedReservation = reservation;
        }

        ReservationStepDefs.currentReservation = reservation;
    }
}