package tqs.backend.service;

import org.springframework.stereotype.Service;
import tqs.backend.model.Charger;
import tqs.backend.model.Station;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import java.util.List;

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
        

}
