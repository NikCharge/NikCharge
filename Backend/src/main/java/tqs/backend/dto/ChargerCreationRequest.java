
package tqs.backend.dto;

import lombok.*;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargerCreationRequest {
    private Long stationId;
    private ChargerType chargerType;
    private ChargerStatus status;
    private BigDecimal pricePerKwh;
}
 
