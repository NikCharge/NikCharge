package tqs.backend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ChargerDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChargerDTO charger = new ChargerDTO();  // <- cobre @NoArgsConstructor

        charger.setId(5L);
        charger.setChargerType(ChargerType.DC_ULTRA_FAST);
        charger.setStatus(ChargerStatus.IN_USE);
        charger.setPricePerKwh(new BigDecimal("0.45"));  // <- cobre @Setter

        assertEquals(5L, charger.getId());
        assertEquals(ChargerType.DC_ULTRA_FAST, charger.getChargerType());
        assertEquals(ChargerStatus.IN_USE, charger.getStatus());
        assertEquals(new BigDecimal("0.45"), charger.getPricePerKwh());
    }
}
