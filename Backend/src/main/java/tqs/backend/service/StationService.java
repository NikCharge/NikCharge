package tqs.backend.service;

import tqs.backend.model.Station;
import tqs.backend.repository.StationRepository;
import tqs.backend.util.GeoUtils;

import java.util.List;
import java.util.stream.Collectors;

public class StationService {

    private final StationRepository repo;

    public StationService(StationRepository repo) {
        this.repo = repo;
    }

    public List<Station> findNearbyStations(double lat, double lng, double radiusKm) {
        return repo.findAll().stream()
                .filter(station -> GeoUtils.calculateDistance(
                        lat, lng,
                        station.getLatitude(), station.getLongitude()
                ) <= radiusKm)
                .collect(Collectors.toList());
    }

    public List<Station> sortByDistance(List<Station> stations, double lat, double lng) {
        return stations.stream()
                .sorted((s1, s2) -> {
                    double d1 = GeoUtils.calculateDistance(lat, lng, s1.getLatitude(), s1.getLongitude());
                    double d2 = GeoUtils.calculateDistance(lat, lng, s2.getLatitude(), s2.getLongitude());
                    return Double.compare(d1, d2);
                })
                .collect(Collectors.toList());
    }

}
