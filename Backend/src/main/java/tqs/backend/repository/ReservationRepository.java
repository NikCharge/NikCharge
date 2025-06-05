package tqs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.backend.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(Long chargerId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.charger c JOIN FETCH c.station WHERE r.user.id = :userId")
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByStartTimeLessThanEqualAndEstimatedEndTimeAfter(LocalDateTime start, LocalDateTime end);
    boolean existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeLessThanEqual(
            Long chargerId,
            LocalDateTime estimatedEndTime,
            LocalDateTime startTime
    );

}
