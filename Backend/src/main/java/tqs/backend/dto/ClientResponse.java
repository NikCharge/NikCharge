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
}
