package tqs.backend.dto;

import lombok.Data;
import tqs.backend.model.enums.ChargerType;

@Data
public class DiscountRequestDTO {
    private Long stationId;
    private ChargerType chargerType;
    private Integer dayOfWeek;
    private Integer startHour;
    private Integer endHour;
    private Double discountPercent;
    private Boolean active;
}
