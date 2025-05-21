package tqs.backend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.model.*;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class StationSummaryDTOTest {

    @Test
    void summaryDtoIncludesCorrectInfo() {
        Station station = new Station();
        station.setName("Test Station");
        station.setLatitude(40.64);
        station.setLongitude(-8.65);

        station.addCharger(new Charger(null, station, ChargerType.DC_FAST, ChargerStatus.AVAILABLE,
                BigDecimal.valueOf(0.30), LocalDateTime.now(), "Model X"));
        station.addCharger(new Charger(null, station, ChargerType.DC_FAST, ChargerStatus.IN_USE,
                BigDecimal.valueOf(0.30), LocalDateTime.now(), "Model X"));

        double userLat = 40.64;
        double userLng = -8.65;

        StationSummaryDTO dto = StationSummaryDTO.fromStation(station, userLat, userLng);

        assertEquals("Test Station", dto.getName());
        assertTrue(dto.getDistance() >= 0);
        assertEquals(1, dto.getAvailableCount());
    }
}
