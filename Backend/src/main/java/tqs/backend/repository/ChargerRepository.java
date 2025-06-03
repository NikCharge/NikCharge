package tqs.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tqs.backend.model.Charger;

import java.util.List;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByStationId(Long stationId);
}
 
