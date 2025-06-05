package tqs.backend.service;

import org.springframework.stereotype.Service;

import tqs.backend.dto.ChargerDTO;
import tqs.backend.dto.StationDetailsDTO;
import tqs.backend.dto.StationRequest;
import tqs.backend.model.Charger;
import tqs.backend.model.Discount;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.StationRepository;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.DiscountRepository;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tqs.backend.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import tqs.backend.model.Reservation;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final DiscountRepository discountRepository;
    private final ReservationRepository reservationRepository;

    public StationService(
            StationRepository stationRepository,
            ChargerRepository chargerRepository,
            DiscountRepository discountRepository,
            ReservationRepository reservationRepository
    ) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.discountRepository = discountRepository;
        this.reservationRepository = reservationRepository;
    }



    // For testing purposes
    public StationRepository getStationRepository() {
        return stationRepository;
    }

    public ChargerRepository getChargerRepository(){
        return chargerRepository;
    }

    public DiscountRepository getDiscountRepository(){
        return discountRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Station getStationById(Long id) {
        return stationRepository.findById(id).orElse(null);
    }

    public Station createStationFromRequest(StationRequest req) {
        Optional<Station> existing = stationRepository.findByLatitudeAndLongitude(
                req.getLatitude(), req.getLongitude());

        if (existing.isPresent()) {
            throw new IllegalStateException("Station already exists at this location");
        }

        Station station = Station.builder()
                .name(req.getName())
                .address(req.getAddress())
                .city(req.getCity())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .build();

        return stationRepository.save(station);
    }

    public StationDetailsDTO getStationDetails(Long stationId, LocalDateTime datetime) {
        Station station = stationRepository.findById(stationId).orElse(null);
        if (station == null) return null;

        List<Charger> chargers = chargerRepository.findByStationId(stationId);

        if (datetime != null) {
            // Fetch reservations that conflict with this datetime
            List<Reservation> reservations = reservationRepository.findByStartTimeBeforeAndEstimatedEndTimeAfter(datetime, datetime);

            Set<Long> reservedChargerIds = reservations.stream()
                    .map(r -> r.getCharger().getId())
                    .collect(Collectors.toSet());

            // Filter out reserved chargers
            chargers = chargers.stream()
                    .filter(c -> !reservedChargerIds.contains(c.getId()))
                    .collect(Collectors.toList());
        }

        List<ChargerDTO> chargerDTOs = chargers.stream()
                .map(c -> ChargerDTO.builder()
                        .id(c.getId())
                        .chargerType(c.getChargerType())
                        .status(c.getStatus()) // Note: still shows DB status
                        .pricePerKwh(c.getPricePerKwh())
                        .build())
                .toList();

        return StationDetailsDTO.builder()
                .id(station.getId())
                .name(station.getName())
                .address(station.getAddress())
                .city(station.getCity())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .chargers(chargerDTOs)
                .build();
    }


    public void deleteStation(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new IllegalArgumentException("Station not found");
        }
        stationRepository.deleteById(id);
    }

    public List<Map<String, Object>> searchStationsWithDiscount(int dayOfWeek, int hour, ChargerType type) {
        List<Station> stations = stationRepository.findAll();
        List<Discount> discounts = discountRepository.findByActiveTrueAndDayOfWeekAndStartHourLessThanEqualAndEndHourGreaterThanEqualAndChargerType(
                dayOfWeek, hour, hour, type
        );

        Map<Long, Discount> stationToDiscount = discounts.stream()
                .collect(Collectors.toMap(d -> d.getStation().getId(), d -> d));

        // Calculate target time window
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.DayOfWeek targetDay = java.time.DayOfWeek.of(dayOfWeek);
        java.time.LocalDateTime targetStart = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay))
                .withHour(hour).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime targetEnd = targetStart.plusHours(1);

        return stations.stream()
                .map(station -> {
                    List<Charger> availableChargers = station.getChargers().stream()
                            .filter(ch -> ch.getChargerType() == type)
                            .filter(ch -> !reservationRepository.existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(
                                    ch.getId(), targetStart, targetEnd))
                            .toList();

                    if (availableChargers.isEmpty()) {
                        return null;
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", station.getId());
                    map.put("name", station.getName());
                    map.put("latitude", station.getLatitude());
                    map.put("longitude", station.getLongitude());
                    map.put("availableChargers", availableChargers.size());

                    if (stationToDiscount.containsKey(station.getId())) {
                        double percent = stationToDiscount.get(station.getId()).getDiscountPercent();
                        map.put("discountTag", String.format("%.0f%% off", percent));
                    }

                    return map;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }


}