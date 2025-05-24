package tqs.backend.dto;

import lombok.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class SignUpRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Battery capacity is required")
    @Positive(message = "Battery capacity must be a positive number")
    private Double batteryCapacityKwh;

    @NotNull(message = "Full range is required")
    @Positive(message = "Full range must be a positive number")
    private Double fullRangeKm;
}
