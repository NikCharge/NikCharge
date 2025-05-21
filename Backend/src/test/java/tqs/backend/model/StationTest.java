package tqs.backend.model;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.ChargerStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StationTest {

    @Test
    void calculatesAvailableChargersCorrectly() {
        Station station = new Station();

        Charger availableCharger = new Charger();
        availableCharger.setStatus(ChargerStatus.AVAILABLE);

        Charger inUseCharger = new Charger();
        inUseCharger.setStatus(ChargerStatus.IN_USE);

        station.addCharger(availableCharger);
        station.addCharger(inUseCharger);

        assertEquals(1, station.getAvailableChargerCount(), "Should count only AVAILABLE chargers");
    }
}
