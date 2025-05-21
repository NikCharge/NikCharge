package tqs.backend.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tqs.backend.model.Station;
import tqs.backend.repository.StationRepository;
import tqs.backend.util.GeoUtils;

public class StationServiceTest {

    private StationRepository stationRepository;
    private StationService stationService;

    @BeforeEach
    void setup() {
        stationRepository = mock(StationRepository.class);
        stationService = new StationService(stationRepository);
    }

    @Test
    void whenUserLocationProvided_thenReturnStationsWithin10Km() {
        double userLat = 40.64;
        double userLng = -8.65;

        Station nearbyStation = new Station(1L, "Nearby Station", "Address A", "City A", 40.65, -8.66, null);
        Station farStation = new Station(2L, "Far Station", "Address B", "City B", 41.20, -8.80, null);

        when(stationRepository.findAll()).thenReturn(Arrays.asList(nearbyStation, farStation));

        List<Station> result = stationService.findNearbyStations(userLat, userLng, 10);

        assertTrue(result.stream().allMatch(station ->
                GeoUtils.calculateDistance(userLat, userLng, station.getLatitude(), station.getLongitude()) <= 10));
    }

    @Test
    void stationsAreSortedByDistance() {
        double userLat = 40.64;
        double userLng = -8.65;

        Station s1 = new Station(1L, "A", "addr", "city", 40.65, -8.66, null); // ~1 km
        Station s2 = new Station(2L, "B", "addr", "city", 40.70, -8.70, null); // ~7 km
        Station s3 = new Station(3L, "C", "addr", "city", 41.00, -8.90, null); // ~45 km

        List<Station> unsorted = Arrays.asList(s2, s3, s1);

        List<Station> sorted = stationService.sortByDistance(unsorted, userLat, userLng);

        assertTrue(!sorted.isEmpty(), "Expected list to not be empty");

        for (int i = 0; i < sorted.size() - 1; i++) {
            double d1 = GeoUtils.calculateDistance(userLat, userLng,
                    sorted.get(i).getLatitude(), sorted.get(i).getLongitude());
            double d2 = GeoUtils.calculateDistance(userLat, userLng,
                    sorted.get(i + 1).getLatitude(), sorted.get(i + 1).getLongitude());

            assertTrue(d1 <= d2, "Stations are not sorted by distance");
        }
    }
}
