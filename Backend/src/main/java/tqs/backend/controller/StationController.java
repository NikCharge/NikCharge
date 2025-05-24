package tqs.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import tqs.backend.dto.StationDetailWithDiscountDTO;
import tqs.backend.dto.StationWithDiscountDTO;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.service.StationService;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @Operation(summary = "GET detalhes da estação com info de carregadores e preços")
    @GetMapping("/{id}")
    public ResponseEntity<?> getStationDetails(@PathVariable Long id) {
        Station station = stationService.getStationDetails(id);
        Map<ChargerType, Long> chargersCount = stationService.getChargersCountByType(id);
        Map<ChargerType, BigDecimal> prices = stationService.getPricePerKwhByType(id);

        Map<String, Object> response = new HashMap<>();
        response.put("id", station.getId());
        response.put("name", station.getName());
        response.put("address", station.getAddress());
        response.put("chargersCount", chargersCount);
        response.put("prices", prices);

        // Real-time availability pode ser preenchido via outro endpoint (ex: /availability)
        // Ou aceitar parâmetros opcionais para horário e filtro
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get disponibilidade em tempo real para uma estação e filtros opcionais")
    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getStationAvailability(
        @PathVariable Long id,
        @RequestParam(required = false) ChargerType chargerType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime
    ) {
        List<Charger> availableChargers = stationService.getAvailableChargers(id, chargerType);

        Map<String, Object> response = new HashMap<>();
        response.put("availableCount", availableChargers.size());
        response.put("availableChargers", availableChargers.stream().map(c -> Map.of(
            "id", c.getId(),
            "type", c.getChargerType(),
            "pricePerKwh", c.getPricePerKwh()
        )).toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "criar nova estação")
    @PostMapping
    public ResponseEntity<Station> createStation(@RequestBody Station station) {
        Station saved = stationService.saveStation(station);
        return ResponseEntity.status(201).body(saved);
    }

    @Operation(summary = "atualizar estação existente")
    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable Long id, @RequestBody Station stationDetails) {
        Station updated = stationService.updateStation(id, stationDetails);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "apagar estação pelo id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "adicionar carregador a uma estação")
    @PostMapping("/{stationId}/chargers")
    public ResponseEntity<Charger> addChargerToStation(@PathVariable Long stationId, @RequestBody Charger charger) {
        Charger saved = stationService.addCharger(stationId, charger);
        return ResponseEntity.status(201).body(saved);
    }

    @Operation(summary = "remover carregador da estação")
    @DeleteMapping("/{stationId}/chargers/{chargerId}")
    public ResponseEntity<Void> removeChargerFromStation(@PathVariable Long stationId, @PathVariable Long chargerId) {
        stationService.removeCharger(stationId, chargerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<StationWithDiscountDTO>> searchStations(
            @RequestParam String city,
            @RequestParam ChargerType chargerType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {

        List<StationWithDiscountDTO> stations = stationService.findStationsWithDiscounts(city, chargerType, dateTime);
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{stationId}")
    public ResponseEntity<StationDetailWithDiscountDTO> getStationDetails(
            @PathVariable Long stationId,
            @RequestParam ChargerType chargerType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {

        StationDetailWithDiscountDTO detail = stationService.getStationDetailsWithDiscount(stationId, chargerType, dateTime);
        return ResponseEntity.ok(detail);
    }

}

