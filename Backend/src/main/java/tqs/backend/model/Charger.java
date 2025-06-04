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
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Charger charger = (Charger) o;
        return java.util.Objects.equals(station, charger.station) &&
               chargerType == charger.chargerType &&
               status == charger.status &&
               java.util.Objects.equals(pricePerKwh, charger.pricePerKwh) &&
               java.util.Objects.equals(lastMaintenance, charger.lastMaintenance) &&
               java.util.Objects.equals(maintenanceNote, charger.maintenanceNote);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(station, chargerType, status, pricePerKwh, lastMaintenance, maintenanceNote);
    }
}