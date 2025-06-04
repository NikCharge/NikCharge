package tqs.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    private Long clientId;
    private Long chargerId;
    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;
    private Double batteryLevelStart;
    private Double estimatedKwh;
    private BigDecimal estimatedCost;
}
