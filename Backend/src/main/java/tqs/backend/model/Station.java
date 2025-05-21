package tqs.backend.model;

import jakarta.persistence.*;

import java.util.List;
import tqs.backend.model.enums.ChargerStatus;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL)
    private List<Charger> chargers;

    public void addCharger(Charger charger) {
        if (this.chargers == null) {
            this.chargers = new java.util.ArrayList<>();
        }
        this.chargers.add(charger);
        charger.setStation(this); // ensure bidirectional mapping is maintained
    }

    public long getAvailableChargerCount() {
        if (this.chargers == null) return 0;
        return chargers.stream()
                .filter(charger -> charger.getStatus() == ChargerStatus.AVAILABLE)
                .count();
    }


}
