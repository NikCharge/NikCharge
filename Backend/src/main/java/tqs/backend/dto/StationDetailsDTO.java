package tqs.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StationDetailsDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private List<ChargerDTO> chargers;
}
