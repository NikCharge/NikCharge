package tqs.backend.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationDetailsDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        StationDetailsDTO dto = new StationDetailsDTO(); // <- cobre @NoArgsConstructor

        dto.setId(10L);
        dto.setName("Estação Alpha");
        dto.setAddress("Rua da Energia, 123");
        dto.setCity("Porto");
        dto.setLatitude(41.1579);
        dto.setLongitude(-8.6291);

        ChargerDTO charger = ChargerDTO.builder()
                .id(1L)
                .chargerType(null)
                .status(null)
                .pricePerKwh(null)
                .build();

        dto.setChargers(List.of(charger)); // <- cobre @Setter

        assertEquals(10L, dto.getId());
        assertEquals("Estação Alpha", dto.getName());
        assertEquals("Rua da Energia, 123", dto.getAddress());
        assertEquals("Porto", dto.getCity());
        assertEquals(41.1579, dto.getLatitude());
        assertEquals(-8.6291, dto.getLongitude());
        assertNotNull(dto.getChargers());
        assertEquals(1, dto.getChargers().size());
        assertEquals(1L, dto.getChargers().get(0).getId());
    }
}
