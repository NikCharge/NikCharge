package tqs.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.backend.dto.ChargerCreationRequest;
import tqs.backend.model.Charger;
import tqs.backend.service.ChargerService;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.dto.ChargerDTO;

@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private static final String ERROR_KEY = "error";

    private final ChargerService chargerService;

    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @PostMapping
    public ResponseEntity<Object> addCharger(@RequestBody ChargerCreationRequest request) {
        try {
            Charger charger = Charger.builder()
                    .chargerType(request.getChargerType())
                    .status(request.getStatus())
                    .pricePerKwh(request.getPricePerKwh())
                    .build();

            Charger created = chargerService.addCharger(request.getStationId(), charger);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Charger>> getAllChargers() {
        return ResponseEntity.ok(chargerService.getAllChargers());
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<Charger>> getChargersByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(chargerService.getChargersForStation(stationId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCharger(@PathVariable Long id) {
        try {
            chargerService.deleteCharger(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @GetMapping("/count/available/total")
    public ResponseEntity<Long> countAvailableChargersTotal() {
        long count = chargerService.countByStatus(ChargerStatus.AVAILABLE);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/available/station/{stationId}")
    public ResponseEntity<Long> countAvailableChargersByStation(@PathVariable Long stationId) {
        long count = chargerService.countByStationAndStatus(stationId, ChargerStatus.AVAILABLE);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/in_use/total")
    public ResponseEntity<Long> countInUseChargersTotal() {
        long count = chargerService.countByStatus(ChargerStatus.IN_USE);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/in_use/station/{stationId}")
    public ResponseEntity<Long> countInUseChargersByStation(@PathVariable Long stationId) {
        long count = chargerService.countByStationAndStatus(stationId, ChargerStatus.IN_USE);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ChargerDTO>> getAvailableChargers() {
        return ResponseEntity.ok(chargerService.getChargersByStatus(ChargerStatus.AVAILABLE));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateChargerStatus(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        try {
            String statusStr = requestBody.get("status");
            String maintenanceNote = requestBody.get("maintenanceNote");

            if (statusStr == null || statusStr.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Missing 'status' in request body"));
            }

            ChargerStatus newStatus;
            try {
                newStatus = parseChargerStatus(statusStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, e.getMessage()));
            }

            if (newStatus != ChargerStatus.UNDER_MAINTENANCE) {
                return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Invalid status value: " + statusStr));
            }

            Charger updatedCharger = chargerService.updateChargerStatus(id, newStatus, maintenanceNote);
            return ResponseEntity.ok(updatedCharger);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    private ChargerStatus parseChargerStatus(String statusStr) {
        try {
            return ChargerStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + statusStr, e);
        }
    }
}
