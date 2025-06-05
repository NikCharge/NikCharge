package tqs.backend.service;

import org.springframework.stereotype.Service;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;
import tqs.backend.dto.ChargerDTO;
import tqs.backend.model.Discount;
import tqs.backend.repository.DiscountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChargerService {

    private final ChargerRepository chargerRepository;
    private final StationRepository stationRepository;
    private final DiscountRepository discountRepository;

    public ChargerService(ChargerRepository chargerRepository, StationRepository stationRepository, DiscountRepository discountRepository) {
        this.chargerRepository = chargerRepository;
        this.stationRepository = stationRepository;
        this.discountRepository = discountRepository;
    }

    public Charger addCharger(Long stationId, Charger charger) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));

        charger.setStation(station);
        return chargerRepository.save(charger);
    }

    public List<Charger> getChargersForStation(Long stationId) {
        return chargerRepository.findByStationId(stationId);
    }

    public List<Charger> getAllChargers() {
        return chargerRepository.findAll();
    }

    public void deleteCharger(Long id) {
        if (!chargerRepository.existsById(id)) {
            throw new IllegalArgumentException("Charger not found");
        }
        chargerRepository.deleteById(id);
    }

    public Optional<Charger> getChargerById(Long id) {
        return chargerRepository.findById(id);
    }

    public Charger saveCharger(Charger charger) {
        return chargerRepository.save(charger);
    }

    public Charger updateChargerStatus(Long id, ChargerStatus status, String maintenanceNote) {
        Charger charger = chargerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Charger not found"));

        charger.setStatus(status);

        // If setting status to AVAILABLE, clear the maintenance note
        if (status == ChargerStatus.AVAILABLE) {
            charger.setMaintenanceNote(null);
        } else {
            // Otherwise, set the maintenance note from the provided argument
            charger.setMaintenanceNote(maintenanceNote);
        }

        return chargerRepository.save(charger);
    }

    public List<ChargerDTO> getChargersByStatus(ChargerStatus status) {
        List<Charger> chargers = chargerRepository.findByStatus(status);
        return chargers.stream()
                .map(charger -> ChargerDTO.builder()
                        .id(charger.getId())
                        .chargerType(charger.getChargerType())
                        .status(charger.getStatus())
                        .pricePerKwh(charger.getPricePerKwh())
                        .stationId(charger.getStation() != null ? charger.getStation().getId() : null)
                        .stationName(charger.getStation() != null ? charger.getStation().getName() : null)
                        .stationCity(charger.getStation() != null ? charger.getStation().getCity() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public long countByStatus(ChargerStatus status) {
        return chargerRepository.countByStatus(status);
    }

    public long countByStationAndStatus(Long stationId, ChargerStatus status) {
        return chargerRepository.countByStationIdAndStatus(stationId, status);
    }

    public BigDecimal calculateChargerPrice(Long chargerId, LocalDateTime reservationTime) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new IllegalArgumentException("Charger not found"));

        // Get the station to find station-level discounts
        Station station = charger.getStation();
        if (station == null) {
            // If a charger somehow doesn't have a station, return its default price
            return charger.getPricePerKwh();
        }

        // Find active discounts for this station applicable at the reservation time
        List<Discount> applicableDiscounts = discountRepository.findByStationAndActiveTrue(station).stream()
                .filter(discount -> isDiscountApplicable(discount, reservationTime))
                .collect(Collectors.toList());

        BigDecimal finalPrice = charger.getPricePerKwh();

        if (!applicableDiscounts.isEmpty()) {
            // Find the maximum discount percentage among applicable discounts
            double maxDiscountPercent = applicableDiscounts.stream()
                    .mapToDouble(Discount::getDiscountPercent)
                    .max()
                    .orElse(0.0);

            // Calculate discounted price
            BigDecimal discountFactor = BigDecimal.valueOf(1.0).subtract(BigDecimal.valueOf(maxDiscountPercent).divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP));
            BigDecimal discountedPrice = charger.getPricePerKwh().multiply(discountFactor);

            // Apply discount, ensuring the price does not exceed the default price
            finalPrice = discountedPrice.min(charger.getPricePerKwh());
        }

        return finalPrice;
    }

    // Helper method to check if a discount is applicable at a given time
    private boolean isDiscountApplicable(Discount discount, LocalDateTime reservationTime) {
        if (discount.getDayOfWeek() == null || discount.getStartHour() == null || discount.getEndHour() == null) {
            return false; // Discount must have a defined time range
        }

        // Check if the day of the week matches
        boolean dayMatches = reservationTime.getDayOfWeek().getValue() == discount.getDayOfWeek();

        // Check if the time falls within the discount hours
        LocalTime reservationLocalTime = reservationTime.toLocalTime();
        LocalTime startOfDiscount = LocalTime.of(discount.getStartHour(), 0);
        LocalTime endOfDiscount = LocalTime.of(discount.getEndHour(), 0);

        // Handle cases where end hour is before start hour (e.g., overnight discount)
        boolean timeMatches;
        if (startOfDiscount.isBefore(endOfDiscount)) {
            // Normal time range (e.g., 9:00 to 17:00)
            timeMatches = !reservationLocalTime.isBefore(startOfDiscount) && reservationLocalTime.isBefore(endOfDiscount);
        } else {
            // Overnight time range (e.g., 22:00 to 6:00)
            // Applicable if time is after start time OR before end time
            timeMatches = !reservationLocalTime.isBefore(startOfDiscount) || reservationLocalTime.isBefore(endOfDiscount);
        }

        return dayMatches && timeMatches;
    }
}