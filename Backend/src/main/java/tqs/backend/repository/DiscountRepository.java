package tqs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tqs.backend.model.Discount;
import tqs.backend.model.enums.ChargerType;
import java.util.*;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByActiveTrueAndDayOfWeekAndStartHourLessThanEqualAndEndHourGreaterThanEqualAndChargerType(
        int dayOfWeek, int startHour, int endHour, ChargerType chargerType
    );
}

