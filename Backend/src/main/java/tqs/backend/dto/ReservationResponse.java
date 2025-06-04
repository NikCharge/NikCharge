package tqs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tqs.backend.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private ChargerDto charger;
    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;
    private Double batteryLevelStart;
    private Double estimatedKwh;
    private BigDecimal estimatedCost;
    private ReservationStatus status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargerDto {
        private Long id;
        private String chargerType;
        private StationDto station;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationDto {
        private Long id;
        private String name;
        private String address;
        private String city;
    }
} 