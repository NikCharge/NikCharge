package tqs.backend.model;

import jakarta.persistence.*;
import tqs.backend.model.enums.ChargerType;

import java.util.List;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EvProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private Double batteryCapacityKwh;
    private Double fullRangeKm;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<ChargerType> chargerCompatibility;
}
