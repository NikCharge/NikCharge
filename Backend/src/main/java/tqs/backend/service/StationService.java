package tqs.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import tqs.backend.dto.StationDetailWithDiscountDTO;
import tqs.backend.dto.StationWithDiscountDTO;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final DiscountService discountService;

    // display: busca estação por id
    public Station getStationDetails(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found"));
    }

    // display: conta carregadores por tipo para uma estação
    public Map<ChargerType, Long> getChargersCountByType(Long stationId) {
        List<Object[]> result = chargerRepository.countChargersByTypeForStation(stationId);
        Map<ChargerType, Long> counts = new HashMap<>();
        for (Object[] row : result) {
            ChargerType type = (ChargerType) row[0];
            Long count = (Long) row[1];
            counts.put(type, count);
        }
        return counts;
    }

    // display: busca carregadores disponíveis para estação e tipo
    public List<Charger> getAvailableChargers(Long stationId, ChargerType chargerType) {
        return chargerRepository.findAvailableChargers(stationId, chargerType);
    }

    // display: retorna preço padrão por tipo de carregador
    public Map<ChargerType, BigDecimal> getPricePerKwhByType(Long stationId) {
        Station station = getStationDetails(stationId);
        Map<ChargerType, BigDecimal> prices = new HashMap<>();
        for (Charger charger : station.getChargers()) {
            prices.putIfAbsent(charger.getChargerType(), charger.getPricePerKwh());
        }
        return prices;
    }

    // display: salvar nova estação
    public Station saveStation(Station station) {
        return stationRepository.save(station);
    }

    // display: atualizar estação existente
    public Station updateStation(Long id, Station stationDetails) {
        Station station = getStationDetails(id);
        station.setName(stationDetails.getName());
        station.setAddress(stationDetails.getAddress());
        // Atualize outros campos conforme necessário
        return stationRepository.save(station);
    }

    // display: apagar estação por id
    public void deleteStation(Long id) {
        stationRepository.deleteById(id);
    }

    // display: adicionar carregador à estação
    public Charger addCharger(Long stationId, Charger charger) {
        Station station = getStationDetails(stationId);
        charger.setStation(station);
        return chargerRepository.save(charger);
    }

    // display: remover carregador da estação
    public void removeCharger(Long stationId, Long chargerId) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new EntityNotFoundException("Charger not found"));
        if (!charger.getStation().getId().equals(stationId)) {
            throw new IllegalArgumentException("Charger does not belong to the specified station");
        }
        chargerRepository.delete(charger);
    }

    public List<StationWithDiscountDTO> findStationsWithDiscounts(String city, ChargerType chargerType, LocalDateTime dateTime) {
        List<Station> stations = stationRepository.findByCity(city);

        List<Long> stationIds = stations.stream()
                .map(Station::getId)
                .collect(Collectors.toList());

        Map<Long, Double> discountsMap = discountService.getActiveDiscountsForStations(stationIds, chargerType, dateTime);

        return stations.stream()
                .map(station -> {
                    Double discount = discountsMap.get(station.getId());
                    return StationWithDiscountDTO.builder()
                            .id(station.getId())
                            .name(station.getName())
                            .address(station.getAddress())
                            .latitude(station.getLatitude())
                            .longitude(station.getLongitude())
                            .discountPercent(discount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca detalhes da estação com desconto (pode ser útil para a view detalhada também)
     */
    public StationDetailWithDiscountDTO getStationDetailsWithDiscount(Long stationId, ChargerType chargerType, LocalDateTime dateTime) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found"));

        Double discount = discountService.getActiveDiscountPercent(stationId, chargerType, dateTime);

        // Contar carregadores por tipo, calcular preços, etc - pode criar métodos utilitários para isso
        Map<ChargerType, Long> chargerCounts = station.getChargers().stream()
                .collect(Collectors.groupingBy(Charger::getChargerType, Collectors.counting()));

        return StationDetailWithDiscountDTO.builder()
                .id(station.getId())
                .name(station.getName())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .chargerCounts(chargerCounts)
                .discountPercent(discount)
                .build();
    }
}
