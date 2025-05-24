package tqs.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import tqs.backend.model.Charger;
import tqs.backend.model.enums.ChargerType;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {

    // Conta carregadores por estação e tipo
    @Query("SELECT c.chargerType AS type, COUNT(c) AS count FROM Charger c WHERE c.station.id = :stationId GROUP BY c.chargerType")
    List<Object[]> countChargersByTypeForStation(@Param("stationId") Long stationId);

    // Buscar carregadores disponíveis em um intervalo para uma estação, possivelmente filtrando por tipo
    @Query("SELECT c FROM Charger c WHERE c.station.id = :stationId AND c.status = 'AVAILABLE' AND (:chargerType IS NULL OR c.chargerType = :chargerType)")
    List<Charger> findAvailableChargers(@Param("stationId") Long stationId, @Param("chargerType") ChargerType chargerType);
}
 
