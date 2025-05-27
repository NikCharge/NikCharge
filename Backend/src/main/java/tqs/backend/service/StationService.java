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
        Optional<Station> existing = stationRepository.findByLatitudeAndLongitude(req.getLatitude(),
                req.getLongitude());
        if (existing.isPresent()) {
            throw new RuntimeException("Station already exists at this location");
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
}
