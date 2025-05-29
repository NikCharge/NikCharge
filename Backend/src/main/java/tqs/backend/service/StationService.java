package tqs.backend.service;

import org.springframework.stereotype.Service;
import tqs.backend.dto.StationRequest;
import tqs.backend.model.Station;
import tqs.backend.repository.StationRepository;

import java.util.Optional;
import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    // For testing purposes
    public StationRepository getStationRepository() {
        return stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Station getStationById(Long id) {
        return stationRepository.findById(id).orElse(null);
    }

    public Station createStationFromRequest(StationRequest req) {
        Optional<Station> existing = stationRepository.findByLatitudeAndLongitude(
                req.getLatitude(), req.getLongitude());

        if (existing.isPresent()) {
            throw new IllegalStateException("Station already exists at this location");
        }

        Station station = Station.builder()
                .name(req.getName())
                .address(req.getAddress())
                .city(req.getCity())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .build();

        return stationRepository.save(station);
    }

    public List<Station> getStationsNear(double lat, double lng, double radiusKm) {
        return stationRepository.findAll().stream()
                .filter(s -> {
                    double distance = haversine(lat, lng, s.getLatitude(), s.getLongitude());
                    s.setDistance(distance); // se tiveres um campo transient `distance`
                    return distance <= radiusKm;
                })
                .toList();
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}
