package tqs.backend.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

import tqs.backend.model.enums.ChargerType;


@Data
@Builder
public class StationDetailWithDiscountDTO {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Map<ChargerType, Long> chargerCounts;
    private Double discountPercent;
   
}
 
