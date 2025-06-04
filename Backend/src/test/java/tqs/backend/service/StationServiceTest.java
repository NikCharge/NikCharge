package tqs.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tqs.backend.dto.StationDetailsDTO;
import tqs.backend.dto.StationRequest;
import tqs.backend.model.Charger;
import tqs.backend.model.Discount;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.StationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StationServiceTest {

    private StationRepository stationRepository;
    private ChargerRepository chargerRepository;
    private DiscountRepository discountRepository;
    private StationService stationService;

    @BeforeEach
    void setup() {
        stationRepository = mock(StationRepository.class);
        chargerRepository = mock(ChargerRepository.class);
        discountRepository = mock(DiscountRepository.class);
        stationService = new StationService(stationRepository, chargerRepository, discountRepository);
    }

    @Test
    void getAllStations_ReturnsList() {
        var station = Station.builder().id(1L).name("S1").build();
        when(stationRepository.findAll()).thenReturn(List.of(station));

        List<Station> result = stationService.getAllStations();

        assertThat(result).hasSize(1).contains(station);
        verify(stationRepository).findAll();
    }

    @Test
    void getStationById_Found() {
        var station = Station.builder().id(2L).name("S2").build();
        when(stationRepository.findById(2L)).thenReturn(Optional.of(station));

        Station result = stationService.getStationById(2L);

        assertThat(result).isEqualTo(station);
        verify(stationRepository).findById(2L);
    }

    @Test
    void getStationById_NotFound() {
        when(stationRepository.findById(3L)).thenReturn(Optional.empty());

        Station result = stationService.getStationById(3L);

        assertThat(result).isNull();
    }

    @Test
    void createStationFromRequest_Success() {
        var req = new StationRequest("New", "Addr", "City", 40.0, -8.0);
        when(stationRepository.findByLatitudeAndLongitude(40.0, -8.0)).thenReturn(Optional.empty());

        var saved = Station.builder().id(10L).name("New").build();
        when(stationRepository.save(any(Station.class))).thenReturn(saved);

        Station result = stationService.createStationFromRequest(req);

        assertThat(result.getId()).isEqualTo(10L);
        verify(stationRepository).save(any());
    }

    @Test
    void createStationFromRequest_Conflict_ThrowsException() {
        var req = new StationRequest("Dup", "Addr", "City", 41.0, -8.0);
        var existing = Station.builder().id(1L).build();
        when(stationRepository.findByLatitudeAndLongitude(41.0, -8.0)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> stationService.createStationFromRequest(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(stationRepository, never()).save(any());
    }

    @Test
    void getStationDetails_ReturnsDetails() {
        var station = Station.builder()
                .id(5L).name("DetailStation").address("Addr").city("Porto").latitude(1.1).longitude(2.2)
                .build();

        var charger = Charger.builder()
                .id(100L)
                .chargerType(ChargerType.DC_FAST)
                .status(ChargerStatus.AVAILABLE)
                .pricePerKwh(BigDecimal.valueOf(0.5)) // CORRIGIDO
                .build();

        when(stationRepository.findById(5L)).thenReturn(Optional.of(station));
        when(chargerRepository.findByStationId(5L)).thenReturn(List.of(charger));

        StationDetailsDTO dto = stationService.getStationDetails(5L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getChargers()).hasSize(1);
        assertThat(dto.getChargers().get(0).getPricePerKwh()).isEqualByComparingTo("0.5"); // CORRIGIDO
    }

    @Test
    void deleteStation_Existing_Deletes() {
        when(stationRepository.existsById(8L)).thenReturn(true);

        stationService.deleteStation(8L);

        verify(stationRepository).deleteById(8L);
    }

    @Test
    void deleteStation_NotExisting_Throws() {
        when(stationRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> stationService.deleteStation(9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Station not found");

        verify(stationRepository, never()).deleteById(any());
    }

    @Test
void searchStationsWithDiscount_ReturnsMappedStations() {
    // Station com desconto
    var discountedStation = Station.builder()
            .id(1L)
            .name("Discounted")
            .latitude(1.1)
            .longitude(2.2)
            .build();

    // Station sem desconto
    var noDiscountStation = Station.builder()
            .id(2L)
            .name("NoDiscount")
            .latitude(3.3)
            .longitude(4.4)
            .build();

    when(stationRepository.findAll()).thenReturn(List.of(discountedStation, noDiscountStation));

    var discount = Discount.builder()
            .station(discountedStation)
            .discountPercent(20.0)
            .chargerType(ChargerType.AC_STANDARD)
            .build();

    when(discountRepository.findByActiveTrueAndDayOfWeekAndStartHourLessThanEqualAndEndHourGreaterThanEqualAndChargerType(
            1, 15, 15, ChargerType.AC_STANDARD))
        .thenReturn(List.of(discount));

    List<Map<String, Object>> result = stationService.searchStationsWithDiscount(1, 15, ChargerType.AC_STANDARD);

    // Verifica que o station com desconto tem a tag de desconto
    assertThat(result).hasSize(2);

    Map<String, Object> discountedMap = result.stream()
            .filter(m -> m.get("id").equals(1L))
            .findFirst().orElseThrow();
    assertThat(discountedMap).containsEntry("name", "Discounted")
                             .containsEntry("discountTag", "20% off");

    // Verifica que o station sem desconto NÃO tem a tag de desconto
    Map<String, Object> noDiscountMap = result.stream()
            .filter(m -> m.get("id").equals(2L))
            .findFirst().orElseThrow();
    assertThat(noDiscountMap).containsEntry("name", "NoDiscount")
                             .doesNotContainKey("discountTag");
}
    
}
