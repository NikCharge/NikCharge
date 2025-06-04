package tqs.backend.model;

import org.junit.jupiter.api.Test;


import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;

class ChargerTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Charger charger = new Charger();
        Station station = new Station(); // pode ser um mock ou instância simples

        charger.setId(1L);
        charger.setStation(station);
        charger.setChargerType(ChargerType.DC_FAST);
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setPricePerKwh(new BigDecimal("0.25"));
        charger.setLastMaintenance(LocalDateTime.of(2024, 12, 1, 10, 0));
        charger.setMaintenanceNote("Maintenance completed successfully");

        assertEquals(1L, charger.getId());
        assertEquals(station, charger.getStation());
        assertEquals(ChargerType.DC_FAST, charger.getChargerType());
        assertEquals(ChargerStatus.AVAILABLE, charger.getStatus());
        assertEquals(new BigDecimal("0.25"), charger.getPricePerKwh());
        assertEquals(LocalDateTime.of(2024, 12, 1, 10, 0), charger.getLastMaintenance());
        assertEquals("Maintenance completed successfully", charger.getMaintenanceNote());
    }

    @Test
    void testAllArgsConstructorAndEqualsHashCode() {
        Station station = new Station();

        Charger charger1 = new Charger(
                1L,
                station,
                ChargerType.DC_FAST,
                ChargerStatus.AVAILABLE,
                new BigDecimal("0.25"),
                LocalDateTime.of(2024, 12, 1, 10, 0),
                "Note A"
        );

        Charger charger2 = new Charger(
                1L,
                station,
                ChargerType.DC_FAST,
                ChargerStatus.AVAILABLE,
                new BigDecimal("0.25"),
                LocalDateTime.of(2024, 12, 1, 10, 0),
                "Note A"
        );

        // Como @EqualsAndHashCode(onlyExplicitlyIncluded = true), equals() depende apenas dos campos anotados.
        // Como nenhum campo está anotado com @EqualsAndHashCode.Include, então equals/hashCode são baseados apenas na referência de objeto,
        // e como são diferentes objetos, o equals será falso por omissão, a não ser que @Data ou @Builder inclua algo.

        // Mas mesmo assim, estamos a cobrir a linha com a anotação.
        assertNotEquals(charger1, charger2);
        assertNotEquals(charger1.hashCode(), charger2.hashCode());
    }

    @Test
    void testBuilder() {
        Charger charger = Charger.builder()
                .id(10L)
                .chargerType(ChargerType.DC_FAST)
                .status(ChargerStatus.UNDER_MAINTENANCE)
                .pricePerKwh(new BigDecimal("0.30"))
                .lastMaintenance(LocalDateTime.now())
                .maintenanceNote("Checking battery sync")
                .build();

        assertEquals(10L, charger.getId());
        assertEquals(ChargerType.DC_FAST, charger.getChargerType());
        assertEquals(ChargerStatus.UNDER_MAINTENANCE, charger.getStatus());
        assertEquals(new BigDecimal("0.30"), charger.getPricePerKwh());
        assertEquals("Checking battery sync", charger.getMaintenanceNote());
    }
}
