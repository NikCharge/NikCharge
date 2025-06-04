package tqs.backend.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Station station = new Station();
        station.setId(1L);
        station.setName("Station Alpha");
        station.setAddress("123 Main St");
        station.setCity("Porto");
        station.setLatitude(41.1579);
        station.setLongitude(-8.6291);
        station.setChargers(List.of());

        assertEquals(1L, station.getId());
        assertEquals("Station Alpha", station.getName());
        assertEquals("123 Main St", station.getAddress());
        assertEquals("Porto", station.getCity());
        assertEquals(41.1579, station.getLatitude());
        assertEquals(-8.6291, station.getLongitude());
        assertNotNull(station.getChargers());
    }

    @Test
    void testAllArgsConstructor() {
        Station station = new Station(
                2L,
                "Beta Station",
                "456 Rua Nova",
                "Lisboa",
                38.7169,
                -9.1399,
                List.of()
        );

        assertEquals(2L, station.getId());
        assertEquals("Beta Station", station.getName());
        assertEquals("456 Rua Nova", station.getAddress());
        assertEquals("Lisboa", station.getCity());
        assertEquals(38.7169, station.getLatitude());
        assertEquals(-9.1399, station.getLongitude());
    }

    @Test
    void testEqualsAndHashCode() {
        Station station1 = Station.builder()
                .latitude(40.0)
                .longitude(-8.0)
                .build();

        Station station2 = Station.builder()
                .latitude(40.0)
                .longitude(-8.0)
                .build();

        Station station3 = Station.builder()
                .latitude(41.0)
                .longitude(-9.0)
                .build();

        assertEquals(station1, station2); // same lat/lon => equals true
        assertEquals(station1.hashCode(), station2.hashCode());

        assertNotEquals(station1, station3); // different lat/lon
        assertNotEquals(station1.hashCode(), station3.hashCode());
    }

    @Test
    void testBuilder() {
        Station station = Station.builder()
                .id(3L)
                .name("Gamma Station")
                .address("789 Avenida")
                .city("Coimbra")
                .latitude(40.2033)
                .longitude(-8.4103)
                .chargers(List.of())
                .build();

        assertEquals("Gamma Station", station.getName());
        assertEquals("Coimbra", station.getCity());
        assertEquals(40.2033, station.getLatitude());
        assertEquals(-8.4103, station.getLongitude());
    }
}
