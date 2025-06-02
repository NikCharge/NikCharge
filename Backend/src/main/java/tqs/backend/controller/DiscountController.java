package tqs.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tqs.backend.model.Discount;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.StationRepository;



@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountRepository discountRepository;
    private final StationRepository stationRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long stationId = Long.valueOf(body.get("stationId").toString());
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));

        Discount discount = Discount.builder()
                .station(station)
                .chargerType(ChargerType.valueOf(body.get("chargerType").toString()))
                .dayOfWeek((Integer) body.get("dayOfWeek"))
                .startHour((Integer) body.get("startHour"))
                .endHour((Integer) body.get("endHour"))
                .discountPercent(Double.valueOf(body.get("discountPercent").toString()))
                .active((Boolean) body.get("active"))
                .build();

        return ResponseEntity.ok(discountRepository.save(discount));
    }
}
