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

@ExtendWith(MockitoExtension.class)
class ChargerServiceTest {

    @Mock
    private ChargerRepository chargerRepository;

    @InjectMocks
    private ChargerService chargerService;

    private Charger charger1;
    private Charger charger2;
    private Charger charger3;

    @BeforeEach
    void setUp() {
        charger1 = new Charger();
        charger1.setId(1L);
        charger1.setStatus(ChargerStatus.AVAILABLE);

        charger2 = new Charger();
        charger2.setId(2L);
        charger2.setStatus(ChargerStatus.IN_USE);

        charger3 = new Charger();
        charger3.setId(3L);
        charger3.setStatus(ChargerStatus.UNDER_MAINTENANCE);
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
        when(chargerRepository.findByStatus(ChargerStatus.AVAILABLE))
                .thenReturn(Arrays.asList(charger1));

        List<Charger> availableChargers = chargerService.getChargersByStatus(ChargerStatus.AVAILABLE);

        assertThat(availableChargers).hasSize(1);
        assertThat(availableChargers.get(0).getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
    }
} 