package tqs.backend.controller;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import tqs.backend.dto.DiscountRequestDTO;
import tqs.backend.model.Discount;
import tqs.backend.service.DiscountService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DiscountRequestDTO dto, HttpServletRequest request) {
        try {
            Discount discount = discountService.createDiscount(
                    dto.getStationId(),
                    dto.getChargerType(),
                    dto.getDayOfWeek(),
                    dto.getStartHour(),
                    dto.getEndHour(),
                    dto.getDiscountPercent(),
                    dto.getActive()
            );
            return ResponseEntity.ok(discount);
        } catch (IllegalArgumentException e) {
            return buildNotFoundResponse(e.getMessage(), request.getRequestURI());
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
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        return discountService.getDiscount(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> buildNotFoundResponse("Discount not found", request.getRequestURI()));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DiscountRequestDTO dto, HttpServletRequest request) {
        try {
            Discount updatedDiscount = discountService.updateDiscount(
                    id,
                    dto.getStationId(),
                    dto.getChargerType(),
                    dto.getDayOfWeek(),
                    dto.getStartHour(),
                    dto.getEndHour(),
                    dto.getDiscountPercent(),
                    dto.getActive()
            );
            return ResponseEntity.ok(updatedDiscount);
        } catch (IllegalArgumentException e) {
            return buildNotFoundResponse(e.getMessage(), request.getRequestURI());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            discountService.deleteDiscount(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return buildNotFoundResponse(e.getMessage(), request.getRequestURI());
        }
    }

    // Método auxiliar para criar resposta 404 com JSON esperado
    private ResponseEntity<Map<String, Object>> buildNotFoundResponse(String message, String path) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", ZonedDateTime.now().toString(),
                "status", 404,
                "error", "Not Found",
                "message", message,
                "path", path
        ));
    }
}
