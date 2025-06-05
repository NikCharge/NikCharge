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
import tqs.backend.dto.ReservationResponse;
import tqs.backend.dto.ReservationResponse.ChargerDto;
import tqs.backend.dto.ReservationResponse.StationDto;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final ChargerRepository chargerRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<ReservationResponse> getReservationsByClientId(Long clientId) {
        List<Reservation> reservations = reservationRepository.findByUserId(clientId);
        return reservations.stream()
                .map(this::convertToDto)
                .toList();
    }

    private ReservationResponse convertToDto(Reservation reservation) {
        StationDto stationDto = null;
        if (reservation.getCharger() != null && reservation.getCharger().getStation() != null) {
            stationDto = new StationDto(
                    reservation.getCharger().getStation().getId(),
                    reservation.getCharger().getStation().getName(),
                    reservation.getCharger().getStation().getAddress(),
                    reservation.getCharger().getStation().getCity()
            );
        }

        ChargerDto chargerDto = null;
        if (reservation.getCharger() != null) {
            chargerDto = new ChargerDto(
                    reservation.getCharger().getId(),
                    reservation.getCharger().getChargerType() != null ? reservation.getCharger().getChargerType().toString() : null,
                    stationDto
            );
        }

        return new ReservationResponse(
                reservation.getId(),
                chargerDto,
                reservation.getStartTime(),
                reservation.getEstimatedEndTime(),
                reservation.getBatteryLevelStart(),
                reservation.getEstimatedKwh(),
                reservation.getEstimatedCost(),
                reservation.getStatus()
        );
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

    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new RuntimeException("Invalid reservation status: only active reservations can be cancelled");
        }

        reservationRepository.delete(reservation);
        return reservation;
    }

    public Reservation completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new RuntimeException("Invalid reservation status: only active reservations can be completed");
        }

        // Assuming completion means setting the end time to now and status to COMPLETED
        reservation.setEstimatedEndTime(LocalDateTime.now()); // Or set actual endTime if available
        reservation.setStatus(ReservationStatus.COMPLETED);

        // TODO: Logic to create/associate ChargingSession and calculate cost if not already done

        return reservationRepository.save(reservation);
    }
}
