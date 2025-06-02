package tqs.backend.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tqs.backend.model.Discount;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.service.DiscountService;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    // CREATE
    @PostMapping
    public ResponseEntity<Discount> create(@RequestBody Map<String, Object> body) {
        try {
            Long stationId = Long.valueOf(body.get("stationId").toString());
            ChargerType chargerType = ChargerType.valueOf(body.get("chargerType").toString());
            Integer dayOfWeek = (Integer) body.get("dayOfWeek");
            Integer startHour = (Integer) body.get("startHour");
            Integer endHour = (Integer) body.get("endHour");
            Double discountPercent = Double.valueOf(body.get("discountPercent").toString());
            Boolean active = (Boolean) body.get("active");

            Discount discount = discountService.createDiscount(stationId, chargerType, dayOfWeek, startHour, endHour, discountPercent, active);
            return ResponseEntity.ok(discount);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // READ all
    @GetMapping
    public ResponseEntity<List<Discount>> getAll() {
        List<Discount> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(discounts);
    }

    // READ one by id
    @GetMapping("/{id}")
    public ResponseEntity<Discount> getById(@PathVariable Long id) {
        return discountService.getDiscount(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount not found"));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Discount> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long stationId = Long.valueOf(body.get("stationId").toString());
            ChargerType chargerType = ChargerType.valueOf(body.get("chargerType").toString());
            Integer dayOfWeek = (Integer) body.get("dayOfWeek");
            Integer startHour = (Integer) body.get("startHour");
            Integer endHour = (Integer) body.get("endHour");
            Double discountPercent = Double.valueOf(body.get("discountPercent").toString());
            Boolean active = (Boolean) body.get("active");

            Discount updatedDiscount = discountService.updateDiscount(id, stationId, chargerType, dayOfWeek, startHour, endHour, discountPercent, active);
            return ResponseEntity.ok(updatedDiscount);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            discountService.deleteDiscount(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
