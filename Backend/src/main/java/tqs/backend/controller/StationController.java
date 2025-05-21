package tqs.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.backend.service.StationService;
import tqs.backend.dto.StationSummaryDTO;
import tqs.backend.model.Station;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    @GetMapping("/nearby")
    public List<StationSummaryDTO> getNearbyStations(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radius
    ) {
        List<Station> nearbyStations = stationService.findNearbyStations(lat, lng, radius);
        List<Station> sorted = stationService.sortByDistance(nearbyStations, lat, lng);

        return sorted.stream()
                .map(station -> StationSummaryDTO.fromStation(station, lat, lng))
                .collect(Collectors.toList());
    }
}
