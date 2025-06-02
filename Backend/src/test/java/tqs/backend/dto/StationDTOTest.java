package tqs.backend.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        StationDTO station = new StationDTO(); // <- cobre @NoArgsConstructor

        station.setId(1L);
        station.setName("Estação Central");
        station.setCity("Lisboa");
        station.setLatitude(38.7169);
        station.setLongitude(-9.1399); // <- cobre @Setter

        assertEquals(1L, station.getId());
        assertEquals("Estação Central", station.getName());
        assertEquals("Lisboa", station.getCity());
        assertEquals(38.7169, station.getLatitude());
        assertEquals(-9.1399, station.getLongitude());
    }
}
