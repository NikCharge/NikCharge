package tqs.backend.model;

import jakarta.persistence.*;
import tqs.backend.model.enums.ChargerType;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Station station;

    @Enumerated(EnumType.STRING)
    private ChargerType chargerType;

    private Integer dayOfWeek;
    private Integer startHour;
    private Integer endHour;

    private Double discountPercent;
    private Boolean active;
}
