package tqs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.backend.model.Charger;
import tqs.backend.model.enums.ChargerStatus;

import java.util.List;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByStationId(Long stationId);
    List<Charger> findByStatus(ChargerStatus status);

    long countByStatus(ChargerStatus status);
    long countByStationIdAndStatus(Long stationId, ChargerStatus status);
}
 
