package tqs.backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tqs.backend.model.Discount;
import tqs.backend.model.enums.ChargerType;

import tqs.backend.repository.*;



@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;

    /**
     * Retorna o desconto ativo para a estação, horário e tipo de carregador,
     * ou null se não houver desconto.
     */
    public Double getActiveDiscountPercent(Long stationId, ChargerType chargerType, LocalDateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek().getValue(); // 1=Monday ... 7=Sunday
        int hour = dateTime.getHour();

        List<Discount> discounts = discountRepository.findActiveDiscountsForStationAtTime(stationId, chargerType, dayOfWeek, hour);
        if (discounts.isEmpty()) {
            return null;
        }
        // Pode retornar o maior desconto ativo
        return discounts.stream()
                .map(Discount::getDiscountPercent)
                .max(Double::compareTo)
                .orElse(null);
    }
    
    /**
     * Retorna um Map<stationId, desconto> para uma lista de estações, para melhorar performance na listagem
     */
    public Map<Long, Double> getActiveDiscountsForStations(List<Long> stationIds, ChargerType chargerType, LocalDateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        int hour = dateTime.getHour();

        List<Discount> discounts = discountRepository.findActiveDiscountsForStationsAtTime(stationIds, chargerType, dayOfWeek, hour);
        Map<Long, Double> discountMap = new HashMap<>();
        for (Discount d : discounts) {
            discountMap.merge(d.getStation().getId(), d.getDiscountPercent(), Double::max);
        }
        return discountMap;
    }
}
