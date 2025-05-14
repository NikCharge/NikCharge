package tqs.backend.model;

import jakarta.persistence.*;

import java.util.List;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String startLocation;
    private String destination;

    private Double batteryLevelStart;

    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL)
    private List<TripPlanStop> stops;
}
