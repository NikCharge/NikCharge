package tqs.backend.model;

import jakarta.persistence.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;

@Entity
@Table(
        name = "station",
        uniqueConstraints = @UniqueConstraint(columnNames = {"latitude", "longitude"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;
    private String address;
    private String city;

    @EqualsAndHashCode.Include
    private Double latitude;

    @EqualsAndHashCode.Include
    private Double longitude;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Charger> chargers;
}