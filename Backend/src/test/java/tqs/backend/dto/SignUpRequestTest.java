package tqs.backend.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignUpRequestTest {

    @Test
    void testEqualsAndHashCode() {
        SignUpRequest a = new SignUpRequest();
        a.setName("Jane");
        a.setEmail("jane@mail.com");
        a.setPassword("pass123");
        a.setBatteryCapacityKwh(70.0);
        a.setFullRangeKm(300.0);

        SignUpRequest b = new SignUpRequest();
        b.setName("Jane");
        b.setEmail("jane@mail.com");
        b.setPassword("pass123");
        b.setBatteryCapacityKwh(70.0);
        b.setFullRangeKm(300.0);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
