package tqs.backend.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRequestTest {

    @Test
    void reservationRequest_shouldStoreFieldsCorrectly() {
        ReservationRequest request = new ReservationRequest();
        request.setClientId(1L);
        request.setChargerId(2L);
        request.setStartTime(LocalDateTime.of(2025, 6, 5, 10, 0));
        request.setEstimatedEndTime(LocalDateTime.of(2025, 6, 5, 11, 0));
        request.setBatteryLevelStart(20.0);
        request.setEstimatedKwh(30.0);
        request.setEstimatedCost(new BigDecimal("6.75"));

        assertThat(request.getClientId()).isEqualTo(1L);
        assertThat(request.getChargerId()).isEqualTo(2L);
        assertThat(request.getStartTime()).isEqualTo("2025-06-05T10:00");
        assertThat(request.getEstimatedEndTime()).isEqualTo("2025-06-05T11:00");
        assertThat(request.getBatteryLevelStart()).isEqualTo(20.0);
        assertThat(request.getEstimatedKwh()).isEqualTo(30.0);
        assertThat(request.getEstimatedCost()).isEqualByComparingTo("6.75");
    }
}
