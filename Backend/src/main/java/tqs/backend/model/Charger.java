package tqs.backend.model;

import jakarta.persistence.*;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    @JsonBackReference
    private Station station;

    @Enumerated(EnumType.STRING)
    private ChargerType chargerType;

    @Enumerated(EnumType.STRING)
    private ChargerStatus status;

    private BigDecimal pricePerKwh;
    private LocalDateTime lastMaintenance;
    private String maintenanceNote;

}