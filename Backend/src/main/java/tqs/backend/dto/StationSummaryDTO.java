// src/main/java/tqs/backend/dto/StationSummaryDTO.java
package tqs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tqs.backend.model.Station;
import tqs.backend.util.GeoUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationSummaryDTO {

    private String name;
    private double distance; // in km
    private long availableCount;

    public static StationSummaryDTO fromStation(Station station, double userLat, double userLng) {
        double dist = GeoUtils.calculateDistance(userLat, userLng,
                station.getLatitude(), station.getLongitude());

        return new StationSummaryDTO(
                station.getName(),
                dist,
                station.getAvailableChargerCount()
        );
    }
}
