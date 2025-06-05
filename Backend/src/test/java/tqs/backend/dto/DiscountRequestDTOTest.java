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
    void testGettersAndSettersWithNull() {
        DiscountRequestDTO dto = new DiscountRequestDTO();

        dto.setStationId(null);
        dto.setChargerType(null);
        dto.setDayOfWeek(null);
        dto.setStartHour(null);
        dto.setEndHour(null);
        dto.setDiscountPercent(null);
        dto.setActive(null);

        assertNull(dto.getStationId());
        assertNull(dto.getChargerType());
        assertNull(dto.getDayOfWeek());
        assertNull(dto.getStartHour());
        assertNull(dto.getEndHour());
        assertNull(dto.getDiscountPercent());
        assertNull(dto.getActive());
    }

    
    @Test
    void testDefaultConstructor() {
        DiscountRequestDTO dto = new DiscountRequestDTO();
        assertNotNull(dto);
    }

    @Test
void testAllArgsConstructor() {
    DiscountRequestDTO dto = new DiscountRequestDTO(
        1L,
        ChargerType.AC_STANDARD,
        1,
        14,
        18,
        15.0,
        true
    );

    assertEquals(1L, dto.getStationId());
    assertEquals(ChargerType.AC_STANDARD, dto.getChargerType());
    assertEquals(1, dto.getDayOfWeek());
    assertEquals(14, dto.getStartHour());
    assertEquals(18, dto.getEndHour());
    assertEquals(15.0, dto.getDiscountPercent());
    assertTrue(dto.getActive());
}

    @Test
    void testBuilder() {
        DiscountRequestDTO dto = DiscountRequestDTO.builder()
            .stationId(1L)
            .chargerType(ChargerType.AC_STANDARD)
            .dayOfWeek(1)
            .startHour(14)
            .endHour(18)
            .discountPercent(15.0)
            .active(true)
            .build();

        assertEquals(1L, dto.getStationId());
        assertEquals(ChargerType.AC_STANDARD, dto.getChargerType());
        assertEquals(1, dto.getDayOfWeek());
        assertEquals(14, dto.getStartHour());
        assertEquals(18, dto.getEndHour());
        assertEquals(15.0, dto.getDiscountPercent());
        assertTrue(dto.getActive());
    }

}
