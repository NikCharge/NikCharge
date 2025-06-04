package tqs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.backend.model.Reservation;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(Long chargerId, LocalDateTime startTime, LocalDateTime endTime);
}
