package tqs.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tqs.backend.model.Discount;
import tqs.backend.model.enums.ChargerType;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    @Query("SELECT d FROM Discount d " +
           "WHERE d.station.id = :stationId " +
           "AND d.chargerType = :chargerType " +
           "AND d.active = true " +
           "AND d.dayOfWeek = :dayOfWeek " +
           "AND :hour BETWEEN d.startHour AND d.endHour")
    List<Discount> findActiveDiscountsForStationAtTime(Long stationId, ChargerType chargerType, Integer dayOfWeek, Integer hour);

    @Query("SELECT d FROM Discount d " +
           "WHERE d.station.id IN :stationIds " +
           "AND d.chargerType = :chargerType " +
           "AND d.active = true " +
           "AND d.dayOfWeek = :dayOfWeek " +
           "AND :hour BETWEEN d.startHour AND d.endHour")
    List<Discount> findActiveDiscountsForStationsAtTime(List<Long> stationIds, ChargerType chargerType, Integer dayOfWeek, Integer hour);
}
 
