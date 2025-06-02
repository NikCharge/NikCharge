package tqs.backend.dto;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StationDTO {
    private Long id;
    private String name;
    private String city;
    private Double latitude;
    private Double longitude;
}
