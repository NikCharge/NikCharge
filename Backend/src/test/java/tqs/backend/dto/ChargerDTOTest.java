package tqs.backend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ChargerDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        // Usa o construtor sem argumentos
        ChargerDTO chargerDTO = new ChargerDTO();

        // Define os valores usando os setters
        chargerDTO.setId(1L);
        chargerDTO.setChargerType(ChargerType.DC_FAST);
        chargerDTO.setStatus(ChargerStatus.AVAILABLE);
        chargerDTO.setPricePerKwh(BigDecimal.valueOf(0.25));
        chargerDTO.setStationId(10L);
        chargerDTO.setStationName("Estação Central");
        chargerDTO.setStationCity("Lisboa");

        // Verifica se os valores foram definidos corretamente
        assertEquals(1L, chargerDTO.getId());
        assertEquals(ChargerType.DC_FAST, chargerDTO.getChargerType());
        assertEquals(ChargerStatus.AVAILABLE, chargerDTO.getStatus());
        assertEquals(BigDecimal.valueOf(0.25), chargerDTO.getPricePerKwh());
        assertEquals(10L, chargerDTO.getStationId());
        assertEquals("Estação Central", chargerDTO.getStationName());
        assertEquals("Lisboa", chargerDTO.getStationCity());
    }
}
