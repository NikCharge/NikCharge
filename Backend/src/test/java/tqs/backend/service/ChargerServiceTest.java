package tqs.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.model.Charger;
import tqs.backend.repository.ChargerRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import tqs.backend.model.enums.ChargerStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import tqs.backend.dto.ChargerDTO;
import tqs.backend.model.Station;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class ChargerServiceTest {

    @Mock
    private ChargerRepository chargerRepository;

    @InjectMocks
    private ChargerService chargerService;

    private Charger charger1;
    private Charger charger2;
    private Charger charger3;

    private Station station1;
    private Station station2;

    @BeforeEach
    void setUp() {
        station1 = new Station();
        station1.setId(1L);
        station1.setName("Test Station 1");
        station1.setCity("Test City 1");
        station1.setAddress("Test Address 1");
        station1.setLatitude(BigDecimal.valueOf(40.123456).doubleValue());
        station1.setLongitude(BigDecimal.valueOf(-8.765432).doubleValue());

        station2 = new Station();
        station2.setId(2L);
        station2.setName("Test Station 2");
        station2.setCity("Test City 2");
        station2.setAddress("Test Address 2");
        station2.setLatitude(BigDecimal.valueOf(41.987654).doubleValue());
        station2.setLongitude(BigDecimal.valueOf(-7.123456).doubleValue());

        charger1 = new Charger();
        charger1.setId(1L);
        charger1.setStatus(ChargerStatus.AVAILABLE);
        charger1.setStation(station1);
        charger1.setChargerType(tqs.backend.model.enums.ChargerType.AC_STANDARD);
        charger1.setPricePerKwh(BigDecimal.valueOf(0.25));

        charger2 = new Charger();
        charger2.setId(2L);
        charger2.setStatus(ChargerStatus.IN_USE);
        charger2.setStation(station1);
        charger2.setChargerType(tqs.backend.model.enums.ChargerType.DC_FAST);
        charger2.setPricePerKwh(BigDecimal.valueOf(0.35));

        charger3 = new Charger();
        charger3.setId(3L);
        charger3.setStatus(ChargerStatus.UNDER_MAINTENANCE);
        charger3.setStation(station2);
        charger3.setChargerType(tqs.backend.model.enums.ChargerType.DC_ULTRA_FAST);
        charger3.setPricePerKwh(BigDecimal.valueOf(0.45));
    }

    @Test
    void whenGetAllChargers_thenReturnAllChargers() {
        when(chargerRepository.findAll()).thenReturn(Arrays.asList(charger1, charger2, charger3));

        List<Charger> found = chargerService.getAllChargers();

        assertThat(found).hasSize(3);
        assertThat(found).extracting(Charger::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void whenGetChargerById_thenReturnCharger() {
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger1));

        Optional<Charger> found = chargerService.getChargerById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(1L);
        assertThat(found.get().getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
    }

    @Test
    void whenSaveCharger_thenReturnSavedCharger() {
        when(chargerRepository.save(any(Charger.class))).thenReturn(charger1);

        Charger saved = chargerService.saveCharger(charger1);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
    }

    @Test
    void whenUpdateChargerStatus_thenReturnUpdatedCharger() {
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger1));
        when(chargerRepository.save(any(Charger.class))).thenReturn(charger1);

        charger1.setStatus(ChargerStatus.IN_USE);
        Charger updated = chargerService.updateChargerStatus(1L, ChargerStatus.IN_USE);

        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(ChargerStatus.IN_USE);
    }

    @Test
    void whenGetChargersByStatus_thenReturnFilteredChargers() {
        // Arrange
        List<Charger> availableChargerEntities = Arrays.asList(charger1);

        when(chargerRepository.findByStatus(ChargerStatus.AVAILABLE))
                .thenReturn(availableChargerEntities);

        // Act
        List<ChargerDTO> availableChargers = chargerService.getChargersByStatus(ChargerStatus.AVAILABLE);

        // Assert
        assertThat(availableChargers).hasSize(1);
        assertThat(availableChargers.get(0).getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
        assertThat(availableChargers.get(0).getStationId()).isEqualTo(station1.getId());
        assertThat(availableChargers.get(0).getStationName()).isEqualTo(station1.getName());
        assertThat(availableChargers.get(0).getStationCity()).isEqualTo(station1.getCity());
    }

    @Test
    void whenCountByStatus_thenReturnCorrectCount() {
        // Arrange
        when(chargerRepository.countByStatus(ChargerStatus.AVAILABLE)).thenReturn(1L);
        when(chargerRepository.countByStatus(ChargerStatus.IN_USE)).thenReturn(1L);
        when(chargerRepository.countByStatus(ChargerStatus.UNDER_MAINTENANCE)).thenReturn(1L);
        when(chargerRepository.countByStatus(ChargerStatus.MAINTENANCE)).thenReturn(0L); // Assuming MAINTENANCE status exists but no chargers have it

        // Act & Assert
        assertThat(chargerService.countByStatus(ChargerStatus.AVAILABLE)).isEqualTo(1L);
        assertThat(chargerService.countByStatus(ChargerStatus.IN_USE)).isEqualTo(1L);
        assertThat(chargerService.countByStatus(ChargerStatus.UNDER_MAINTENANCE)).isEqualTo(1L);
        assertThat(chargerService.countByStatus(ChargerStatus.MAINTENANCE)).isEqualTo(0L);
    }

    @Test
    void whenCountByStationAndStatus_thenReturnCorrectCount() {
        // Arrange
        Long station1Id = station1.getId();
        Long station2Id = station2.getId();

        when(chargerRepository.countByStationIdAndStatus(station1Id, ChargerStatus.AVAILABLE)).thenReturn(1L);
        when(chargerRepository.countByStationIdAndStatus(station1Id, ChargerStatus.IN_USE)).thenReturn(1L);
        when(chargerRepository.countByStationIdAndStatus(station1Id, ChargerStatus.UNDER_MAINTENANCE)).thenReturn(0L);

        when(chargerRepository.countByStationIdAndStatus(station2Id, ChargerStatus.AVAILABLE)).thenReturn(0L);
        when(chargerRepository.countByStationIdAndStatus(station2Id, ChargerStatus.IN_USE)).thenReturn(0L);
        when(chargerRepository.countByStationIdAndStatus(station2Id, ChargerStatus.UNDER_MAINTENANCE)).thenReturn(1L);

        // Act & Assert
        assertThat(chargerService.countByStationAndStatus(station1Id, ChargerStatus.AVAILABLE)).isEqualTo(1L);
        assertThat(chargerService.countByStationAndStatus(station1Id, ChargerStatus.IN_USE)).isEqualTo(1L);
        assertThat(chargerService.countByStationAndStatus(station1Id, ChargerStatus.UNDER_MAINTENANCE)).isEqualTo(0L);

        assertThat(chargerService.countByStationAndStatus(station2Id, ChargerStatus.AVAILABLE)).isEqualTo(0L);
        assertThat(chargerService.countByStationAndStatus(station2Id, ChargerStatus.IN_USE)).isEqualTo(0L);
        assertThat(chargerService.countByStationAndStatus(station2Id, ChargerStatus.UNDER_MAINTENANCE)).isEqualTo(1L);

        // Test a station with no chargers
        when(chargerRepository.countByStationIdAndStatus(3L, ChargerStatus.AVAILABLE)).thenReturn(0L);
        assertThat(chargerService.countByStationAndStatus(3L, ChargerStatus.AVAILABLE)).isEqualTo(0L);
    }
} 