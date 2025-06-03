package tqs.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import tqs.backend.dto.StationDTO;
import tqs.backend.dto.StationDetailsDTO;
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
    public ResponseEntity<List<StationDTO>> getAllStations() {
        List<StationDTO> stations = stationService.getAllStations()
                .stream()
                .map(station -> StationDTO.builder()
                        .id(station.getId())
                        .name(station.getName())
                        .city(station.getCity())
                        .latitude(station.getLatitude())
                        .longitude(station.getLongitude())
                        .build()
                ).toList();

        return ResponseEntity.ok(stations);
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
    public ResponseEntity<Object> createStation(
            @Valid @RequestBody StationRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, errors));
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

    @GetMapping("/{id}/details")
    public ResponseEntity<Object> getStationDetails(@PathVariable Long id) {
        StationDetailsDTO dto = stationService.getStationDetails(id);
        if (dto == null) {
            // Usar ERROR_KEY em vez de literal "error"
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, "Station not found"));
        }
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteStation(@PathVariable Long id) {
        try {
            stationService.deleteStation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Usar ERROR_KEY em vez de literal "error"
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

}