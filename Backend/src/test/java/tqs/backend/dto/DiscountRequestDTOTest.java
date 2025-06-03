package tqs.backend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.ChargerType;

import static org.junit.jupiter.api.Assertions.*;

class DiscountRequestDTOTest {

    @Test
    void testGetterAndSetter() {
        DiscountRequestDTO dto = new DiscountRequestDTO();

        dto.setStationId(1L);
        dto.setChargerType(ChargerType.AC_STANDARD);
        dto.setDayOfWeek(1);
        dto.setStartHour(14);
        dto.setEndHour(18);
        dto.setDiscountPercent(15.0);
        dto.setActive(true);

        assertEquals(1L, dto.getStationId());
        assertEquals(ChargerType.AC_STANDARD, dto.getChargerType());
        assertEquals(1, dto.getDayOfWeek());
        assertEquals(14, dto.getStartHour());
        assertEquals(18, dto.getEndHour());
        assertEquals(15.0, dto.getDiscountPercent());
        assertTrue(dto.getActive());
    }

    @Test
    void testToStringEqualsHashCode() {
        DiscountRequestDTO dto1 = new DiscountRequestDTO();
        dto1.setStationId(1L);
        dto1.setChargerType(ChargerType.DC_FAST);
        dto1.setDayOfWeek(2);
        dto1.setStartHour(10);
        dto1.setEndHour(12);
        dto1.setDiscountPercent(10.0);
        dto1.setActive(false);

        DiscountRequestDTO dto2 = new DiscountRequestDTO();
        dto2.setStationId(1L);
        dto2.setChargerType(ChargerType.DC_FAST);
        dto2.setDayOfWeek(2);
        dto2.setStartHour(10);
        dto2.setEndHour(12);
        dto2.setDiscountPercent(10.0);
        dto2.setActive(false);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
    }
}
