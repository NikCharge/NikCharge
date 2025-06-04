package tqs.backend.dto;

import lombok.*;
import tqs.backend.model.enums.ChargerType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiscountRequestDTO {
    private Long stationId;
    private ChargerType chargerType;
    private Integer dayOfWeek;
    private Integer startHour;
    private Integer endHour;
    private Double discountPercent;
    private Boolean active;
}
