package tqs.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StationWithDiscountDTO {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double discountPercent;
}
