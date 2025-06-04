package tqs.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tqs.backend.dto.ReservationRequest;
import tqs.backend.model.Charger;
import tqs.backend.model.Reservation;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ReservationStatus;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.ReservationRepository;
import tqs.backend.model.Client;
import tqs.backend.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final ChargerRepository chargerRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public boolean hasOverlappingReservation(Long chargerId, LocalDateTime startTime, LocalDateTime endTime) {
        return reservationRepository.existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(chargerId, startTime, endTime);
    }

    public Reservation createReservation(ReservationRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Charger charger = chargerRepository.findById(request.getChargerId())
                .orElseThrow(() -> new RuntimeException("Charger not found"));

        // 🔒 Prevent reservation if charger is in maintenance
        if (charger.getStatus() == ChargerStatus.UNDER_MAINTENANCE) {
            throw new RuntimeException("This charger is currently under maintenance and cannot be reserved.");
        }

        // Check for overlapping reservations
        if (hasOverlappingReservation(charger.getId(), request.getStartTime(), request.getEstimatedEndTime())) {
            throw new RuntimeException("Charger is already reserved for the requested time.");
        }

        Reservation reservation = Reservation.builder()
                .user(client)
                .charger(charger)
                .startTime(request.getStartTime())
                .estimatedEndTime(request.getEstimatedEndTime())
                .batteryLevelStart(request.getBatteryLevelStart())
                .estimatedKwh(request.getEstimatedKwh())
                .estimatedCost(request.getEstimatedCost())
                .status(ReservationStatus.ACTIVE)
                .build();

        return reservationRepository.save(reservation);
    }
}
