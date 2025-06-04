package tqs.backend.service;

import org.springframework.stereotype.Service;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ChargerService {

    private final ChargerRepository chargerRepository;
    private final StationRepository stationRepository;

    public ChargerService(ChargerRepository chargerRepository, StationRepository stationRepository) {
        this.chargerRepository = chargerRepository;
        this.stationRepository = stationRepository;
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

    public Charger updateChargerStatus(Long id, ChargerStatus status) {
        Charger charger = chargerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Charger not found"));
        charger.setStatus(status);
        return chargerRepository.save(charger);
    }

    public List<Charger> getChargersByStatus(ChargerStatus status) {
        return chargerRepository.findByStatus(status);
    }

    public long countByStatus(ChargerStatus status) {
        return chargerRepository.countByStatus(status);
    }

    public long countByStationAndStatus(Long stationId, ChargerStatus status) {
        return chargerRepository.countByStationIdAndStatus(stationId, status);
    }
}
