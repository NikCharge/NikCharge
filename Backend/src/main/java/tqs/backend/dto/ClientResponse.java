package tqs.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tqs.backend.model.enums.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("batteryCapacityKwh")
    private Double batteryCapacityKwh;
    
    @JsonProperty("fullRangeKm")
    private Double fullRangeKm;
    
    @JsonProperty("role")
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
