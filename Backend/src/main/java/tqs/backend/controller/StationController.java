package tqs.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tqs.backend.dto.StationRequest;
import tqs.backend.model.Station;
import tqs.backend.service.StationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private static final String ERROR_KEY = "error";

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<List<Station>> getStations(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10") Double radiusKm
    ) {
        if (lat != null && lng != null) {
            List<Station> nearbyStations = stationService.getStationsNear(lat, lng, radiusKm);
            return ResponseEntity.ok(nearbyStations);
        } else {
            List<Station> stations = stationService.getAllStations();
            return ResponseEntity.ok(stations);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getStationById(@PathVariable Long id) {
        Station station = stationService.getStationById(id);
        if (station == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, "Station not found"));
        }
        return ResponseEntity.ok(station);
    }

    @PostMapping
    public ResponseEntity<Object> createStation(@Valid @RequestBody StationRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, errors));
        }

        try {
            Station created = stationService.createStationFromRequest(request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            if ("Station already exists at this location".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(ERROR_KEY, "Station already exists at this location"));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Unexpected error: " + e.getMessage()));
        }
    }
}
