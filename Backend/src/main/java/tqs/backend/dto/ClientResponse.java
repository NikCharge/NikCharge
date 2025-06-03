package tqs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tqs.backend.model.enums.UserRole;

@Data
@AllArgsConstructor
public class ClientResponse {
    private String email;
    private String name;
    private Double batteryCapacityKwh;
    private Double fullRangeKm;
    private UserRole role;

    // Constructor without role for backward compatibility
    public ClientResponse(String email, String name, Double batteryCapacityKwh, Double fullRangeKm) {
        this.email = email;
        this.name = name;
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.fullRangeKm = fullRangeKm;
        this.role = UserRole.CLIENT; // Default role
    }
}
