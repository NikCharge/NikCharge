package tqs.backend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationResponseTest {

    @Test
    void testReservationResponseGettersAndSetters() {
        ReservationResponse response = new ReservationResponse();

        Long id = 1L;
        ReservationResponse.ChargerDto charger = new ReservationResponse.ChargerDto();
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime estimatedEndTime = startTime.plusHours(1);
        Double batteryLevelStart = 20.0;
        Double estimatedKwh = 30.0;
        BigDecimal estimatedCost = new BigDecimal("15.00");
        ReservationStatus status = ReservationStatus.ACTIVE;

        response.setId(id);
        response.setCharger(charger);
        response.setStartTime(startTime);
        response.setEstimatedEndTime(estimatedEndTime);
        response.setBatteryLevelStart(batteryLevelStart);
        response.setEstimatedKwh(estimatedKwh);
        response.setEstimatedCost(estimatedCost);
        response.setStatus(status);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getCharger()).isEqualTo(charger);
        assertThat(response.getStartTime()).isEqualTo(startTime);
        assertThat(response.getEstimatedEndTime()).isEqualTo(estimatedEndTime);
        assertThat(response.getBatteryLevelStart()).isEqualTo(batteryLevelStart);
        assertThat(response.getEstimatedKwh()).isEqualTo(estimatedKwh);
        assertThat(response.getEstimatedCost()).isEqualTo(estimatedCost);
        assertThat(response.getStatus()).isEqualTo(status);
    }

    @Test
    void testChargerDtoGettersAndSetters() {
        ReservationResponse.ChargerDto chargerDto = new ReservationResponse.ChargerDto();

        Long id = 101L;
        String chargerType = "DC_FAST";
        ReservationResponse.StationDto station = new ReservationResponse.StationDto();

        chargerDto.setId(id);
        chargerDto.setChargerType(chargerType);
        chargerDto.setStation(station);

        assertThat(chargerDto.getId()).isEqualTo(id);
        assertThat(chargerDto.getChargerType()).isEqualTo(chargerType);
        assertThat(chargerDto.getStation()).isEqualTo(station);
    }

    @Test
    void testStationDtoGettersAndSetters() {
        ReservationResponse.StationDto stationDto = new ReservationResponse.StationDto();

        Long id = 1L;
        String name = "Test Station";
        String address = "123 Test St";
        String city = "Test City";

        stationDto.setId(id);
        stationDto.setName(name);
        stationDto.setAddress(address);
        stationDto.setCity(city);

        assertThat(stationDto.getId()).isEqualTo(id);
        assertThat(stationDto.getName()).isEqualTo(name);
        assertThat(stationDto.getAddress()).isEqualTo(address);
        assertThat(stationDto.getCity()).isEqualTo(city);
    }
} 