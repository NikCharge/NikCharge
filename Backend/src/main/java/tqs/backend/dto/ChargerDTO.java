package tqs.backend.dto;

import lombok.*;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargerDTO {
    private Long id;
    private ChargerType chargerType;
    private ChargerStatus status;
    private BigDecimal pricePerKwh;
}
