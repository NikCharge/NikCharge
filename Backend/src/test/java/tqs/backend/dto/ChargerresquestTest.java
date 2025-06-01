package tqs.backend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ChargerCreationRequestTest {

    @Test
    void testAllArgsConstructor() {
        ChargerCreationRequest request = new ChargerCreationRequest(
            1L,
            ChargerType.DC_FAST,
            ChargerStatus.AVAILABLE,
            new BigDecimal("0.30")
        );

        assertEquals(1L, request.getStationId());
        assertEquals(ChargerType.DC_FAST, request.getChargerType());
        assertEquals(ChargerStatus.AVAILABLE, request.getStatus());
        assertEquals(new BigDecimal("0.30"), request.getPricePerKwh());
    }

    @Test
    void testBuilder() {
        ChargerCreationRequest request = ChargerCreationRequest.builder()
            .stationId(2L)
            .chargerType(ChargerType.AC_STANDARD)
            .status(ChargerStatus.IN_USE)
            .pricePerKwh(new BigDecimal("0.20"))
            .build();

        assertEquals(2L, request.getStationId());
        assertEquals(ChargerType.AC_STANDARD, request.getChargerType());
        assertEquals(ChargerStatus.IN_USE, request.getStatus());
        assertEquals(new BigDecimal("0.20"), request.getPricePerKwh());
    }
}
