package tqs.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeoUtilsTest {

    @Test
    void returnsAccurateGeoDistance() {
        double km = GeoUtils.calculateDistance(
                40.63, -8.65,  // Aveiro
                40.64, -8.65   // Slightly north
        );

        assertTrue(km > 0 && km < 5, "Expected distance to be small but positive");
    }
}
