package tqs.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.model.Discount;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.StationRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final StationRepository stationRepository;

    public DiscountService(DiscountRepository discountRepository, StationRepository stationRepository) {
        this.discountRepository = discountRepository;
        this.stationRepository = stationRepository;
    }

    public Discount createDiscount(Long stationId, ChargerType chargerType, Integer dayOfWeek, Integer startHour,
                                   Integer endHour, Double discountPercent, Boolean active) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));

        Discount discount = Discount.builder()
                .station(station)
                .chargerType(chargerType)
                .dayOfWeek(dayOfWeek)
                .startHour(startHour)
                .endHour(endHour)
                .discountPercent(discountPercent)
                .active(active)
                .build();

        return discountRepository.save(discount);
    }

    public Optional<Discount> getDiscount(Long id) {
        return discountRepository.findById(id);
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public Discount updateDiscount(Long id, Long stationId, ChargerType chargerType, Integer dayOfWeek, Integer startHour,
                                   Integer endHour, Double discountPercent, Boolean active) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found"));

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));

        discount.setStation(station);
        discount.setChargerType(chargerType);
        discount.setDayOfWeek(dayOfWeek);
        discount.setStartHour(startHour);
        discount.setEndHour(endHour);
        discount.setDiscountPercent(discountPercent);
        discount.setActive(active);

        return discountRepository.save(discount);
    }

    public void deleteDiscount(Long id) {
        if (!discountRepository.existsById(id)) {
            throw new IllegalArgumentException("Discount not found");
        }
        discountRepository.deleteById(id);
    }
}
