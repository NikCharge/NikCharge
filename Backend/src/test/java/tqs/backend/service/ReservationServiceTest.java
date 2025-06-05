package tqs.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.dto.ReservationRequest;
import tqs.backend.dto.ReservationResponse;
import tqs.backend.model.Charger;
import tqs.backend.model.Client;
import tqs.backend.model.Reservation;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ReservationStatus;
import tqs.backend.repository.ChargerRepository;
import tqs.backend.repository.ClientRepository;
import tqs.backend.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ChargerRepository chargerRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Client client;
    private Charger charger;
    private Reservation reservation;
    private ReservationRequest request;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Station station;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now();
        endTime = startTime.plusHours(2);

        client = Client.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        station = Station.builder()
                .id(1L)
                .name("Test Station")
                .address("123 Test St")
                .city("Test City")
                .build();

        charger = Charger.builder()
                .id(1L)
                .status(ChargerStatus.AVAILABLE)
                .station(station)
                .chargerType(tqs.backend.model.enums.ChargerType.AC_STANDARD)
                .build();

        reservation = Reservation.builder()
                .id(1L)
                .user(client)
                .charger(charger)
                .startTime(startTime)
                .estimatedEndTime(endTime)
                .batteryLevelStart(20.0)
                .estimatedKwh(30.0)
                .estimatedCost(new BigDecimal("15.00"))
                .status(ReservationStatus.ACTIVE)
                .build();

        request = new ReservationRequest();
        request.setClientId(1L);
        request.setChargerId(1L);
        request.setStartTime(startTime);
        request.setEstimatedEndTime(endTime);
        request.setBatteryLevelStart(20.0);
        request.setEstimatedKwh(30.0);
        request.setEstimatedCost(new BigDecimal("15.00"));
    }

    @Test
    void whenGetAllReservations_thenReturnAllReservations() {
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(reservation));

        List<Reservation> found = reservationService.getAllReservations();

        assertThat(found).hasSize(1);
        assertThat(found.get(0)).isEqualTo(reservation);
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void whenHasOverlappingReservation_thenReturnTrue() {
        when(reservationRepository.existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(
                any(), any(), any())).thenReturn(true);

        boolean hasOverlap = reservationService.hasOverlappingReservation(1L, startTime, endTime);

        assertThat(hasOverlap).isTrue();
        verify(reservationRepository, times(1))
                .existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(1L, startTime, endTime);
    }

    @Test
    void whenCreateReservation_thenReturnCreatedReservation() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger));
        when(reservationRepository.existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(
                any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Reservation created = reservationService.createReservation(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void whenCreateReservationWithNonExistentClient_thenThrowException() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    void whenCreateReservationWithNonExistentCharger_thenThrowException() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(chargerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Charger not found");
    }

    @Test
    void whenCreateReservationWithMaintenanceCharger_thenThrowException() {
        charger.setStatus(ChargerStatus.UNDER_MAINTENANCE);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("under maintenance");
    }

    @Test
    void whenCreateReservationWithOverlappingTime_thenThrowException() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger));
        when(reservationRepository.existsByChargerIdAndEstimatedEndTimeAfterAndStartTimeBefore(
                any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already reserved");
    }

    @Test
    void whenGetReservationsByClientId_thenReturnListOfReservationResponses() {
        when(reservationRepository.findByUserId(1L)).thenReturn(Arrays.asList(reservation));

        List<ReservationResponse> found = reservationService.getReservationsByClientId(1L);

        assertThat(found).hasSize(1);
        ReservationResponse response = found.get(0);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(reservation.getId());
        assertThat(response.getStartTime()).isEqualTo(reservation.getStartTime());
        assertThat(response.getEstimatedEndTime()).isEqualTo(reservation.getEstimatedEndTime());
        assertThat(response.getBatteryLevelStart()).isEqualTo(reservation.getBatteryLevelStart());
        assertThat(response.getEstimatedKwh()).isEqualTo(reservation.getEstimatedKwh());
        assertThat(response.getEstimatedCost()).isEqualTo(reservation.getEstimatedCost());
        assertThat(response.getStatus()).isEqualTo(reservation.getStatus());

        assertThat(response.getCharger()).isNotNull();
        assertThat(response.getCharger().getId()).isEqualTo(charger.getId());
        assertThat(response.getCharger().getChargerType()).isEqualTo(charger.getChargerType().toString());

        assertThat(response.getCharger().getStation()).isNotNull();
        assertThat(response.getCharger().getStation().getId()).isEqualTo(station.getId());
        assertThat(response.getCharger().getStation().getName()).isEqualTo(station.getName());
        assertThat(response.getCharger().getStation().getAddress()).isEqualTo(station.getAddress());
        assertThat(response.getCharger().getStation().getCity()).isEqualTo(station.getCity());

        verify(reservationRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("Cancel an active reservation successfully")
    void whenCancelActiveReservation_thenDeleteAndReturnReservation() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act
        Reservation result = reservationService.cancelReservation(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    @DisplayName("Attempt to cancel non-existent reservation")
    void whenCancelNonExistentReservation_thenThrowException() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reservationService.cancelReservation(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reservation not found");

        verify(reservationRepository, times(1)).findById(999L);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Attempt to cancel already completed reservation")
    void whenCancelCompletedReservation_thenThrowException() {
        // Arrange
        reservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid reservation status: only active reservations can be cancelled");

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, never()).delete(any());
    }
} 