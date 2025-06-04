package tqs.backend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReservationResponseTest {

    @Test
    void testReservationResponseDto() {
        // Create StationDto
        ReservationResponse.StationDto stationDto = new ReservationResponse.StationDto(
                1L,
                "Test Station",
                "123 Test St",
                "Test City"
        );

        // Create ChargerDto
        ReservationResponse.ChargerDto chargerDto = new ReservationResponse.ChargerDto(
                101L,
                "DC_FAST",
                stationDto
        );

        // Create ReservationResponse
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);
        BigDecimal estimatedCost = new BigDecimal("10.50");

        ReservationResponse reservationResponse = new ReservationResponse(
                201L,
                chargerDto,
                startTime,
                endTime,
                80.0,
                50.0,
                estimatedCost,
                ReservationStatus.ACTIVE
        );

        // Verify ReservationResponse fields
        assertNotNull(reservationResponse);
        assertEquals(201L, reservationResponse.getId());
        assertEquals(startTime, reservationResponse.getStartTime());
        assertEquals(endTime, reservationResponse.getEstimatedEndTime());
        assertEquals(80.0, reservationResponse.getBatteryLevelStart());
        assertEquals(50.0, reservationResponse.getEstimatedKwh());
        assertEquals(estimatedCost, reservationResponse.getEstimatedCost());
        assertEquals(ReservationStatus.ACTIVE, reservationResponse.getStatus());

        // Verify ChargerDto fields within ReservationResponse
        assertNotNull(reservationResponse.getCharger());
        assertEquals(101L, reservationResponse.getCharger().getId());
        assertEquals("DC_FAST", reservationResponse.getCharger().getChargerType());

        // Verify StationDto fields within ChargerDto
        assertNotNull(reservationResponse.getCharger().getStation());
        assertEquals(1L, reservationResponse.getCharger().getStation().getId());
        assertEquals("Test Station", reservationResponse.getCharger().getStation().getName());
        assertEquals("123 Test St", reservationResponse.getCharger().getStation().getAddress());
        assertEquals("Test City", reservationResponse.getCharger().getStation().getCity());
    }
} 