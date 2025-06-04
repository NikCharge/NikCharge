package tqs.backend.dto;

import lombok.*;
import java.util.List;
import tqs.backend.model.Reservation;
import tqs.backend.model.enums.UserRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private Long id;
    private String email;
    private String name;
    private Double batteryCapacityKwh;
    private Double fullRangeKm;
    private List<Reservation> reservations;
    private UserRole role;
//
//    // Constructor without role for backward compatibility
//    @Builder(builderMethodName = "simpleBuilder")
//    public ClientResponse(String email, String name, Double batteryCapacityKwh, Double fullRangeKm) {
//        this.email = email;
//        this.name = name;
//        this.batteryCapacityKwh = batteryCapacityKwh;
//        this.fullRangeKm = fullRangeKm;
//        this.role = UserRole.CLIENT; // Default role
//    }
}
