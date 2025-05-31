package tqs.backend.controller;


import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.backend.dto.ChargerCreationRequest;
import tqs.backend.model.Charger;
import tqs.backend.service.ChargerService;

@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final ChargerService chargerService;

    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @PostMapping
    public ResponseEntity<?> addCharger(@RequestBody ChargerCreationRequest request) {
        try {
            Charger charger = Charger.builder()
                    .chargerType(request.getChargerType())
                    .status(request.getStatus())
                    .pricePerKwh(request.getPricePerKwh())
                    .build();

            Charger created = chargerService.addCharger(request.getStationId(), charger);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", e.getMessage())
            );
        }
    }

    // GET /api/chargers
    @GetMapping
    public ResponseEntity<?> getAllChargers() {
        return ResponseEntity.ok(chargerService.getAllChargers());
    }

    // GET /api/chargers/station/{stationId}
    @GetMapping("/station/{stationId}")
    public ResponseEntity<?> getChargersByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(chargerService.getChargersForStation(stationId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCharger(@PathVariable Long id) {
        try {
            chargerService.deleteCharger(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


}
 