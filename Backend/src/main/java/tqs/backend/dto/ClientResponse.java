package tqs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientResponse {
    private String email;
    private String name;
    private Double batteryCapacityKwh;
    private Double fullRangeKm;
}
