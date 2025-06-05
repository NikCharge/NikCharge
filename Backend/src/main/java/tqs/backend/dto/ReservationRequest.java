package tqs.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReservationRequest {
    private Long clientId;
    private Long chargerId;
    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;
    private Double batteryLevelStart;
    private Double estimatedKwh;
    private BigDecimal estimatedCost;
}
