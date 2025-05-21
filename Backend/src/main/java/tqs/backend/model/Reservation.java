package tqs.backend.model;

import jakarta.persistence.*;
import tqs.backend.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Charger charger;

    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;

    private Double batteryLevelStart;
    private Double estimatedKwh;
    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
}
