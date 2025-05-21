package tqs.backend.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String name;
    private String email;
    private String password;
    private Double batteryCapacityKwh;
    private Double fullRangeKm;
}
