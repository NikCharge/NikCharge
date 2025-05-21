package tqs.backend.dto;

import lombok.*;
import tqs.backend.model.Station;
import tqs.backend.util.GeoUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StationSummaryDTO {
    private String name;
    private double distance;
    private int availableCount;

    public static StationSummaryDTO fromStation(Station station, double userLat, double userLng) {
        double dist = GeoUtils.calculateDistance(
                userLat, userLng,
                station.getLatitude(), station.getLongitude()
        );
        return new StationSummaryDTO(
                station.getName(),
                dist,
                (int) station.getAvailableChargerCount()
        );
    }
}
