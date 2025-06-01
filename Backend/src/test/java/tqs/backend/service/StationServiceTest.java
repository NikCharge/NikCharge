package tqs.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tqs.backend.dto.StationDetailsDTO;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.StationRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StationServiceTest {

    private StationRepository stationRepository;
    private ChargerRepository chargerRepository;
    private StationService stationService;

    @BeforeEach
    void setup() {
        stationRepository = mock(StationRepository.class);
        chargerRepository = mock(ChargerRepository.class);
        stationService = new StationService(stationRepository, chargerRepository);
    }

    @Test
    void getStationDetails_ReturnsNull_WhenStationNotFound() {
        // Given
        Long stationId = 999L;
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        // When
        StationDetailsDTO result = stationService.getStationDetails(stationId);

        // Then
        assertThat(result).isNull(); // cobre o if (station == null) return null;
        verify(stationRepository, times(1)).findById(stationId);
        verifyNoMoreInteractions(stationRepository);
        verifyNoInteractions(chargerRepository);
    }
}
