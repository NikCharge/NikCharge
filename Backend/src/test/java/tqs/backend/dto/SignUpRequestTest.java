package tqs.backend.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignUpRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        SignUpRequest request = new SignUpRequest();

        request.setName("Ana Silva");
        request.setEmail("ana.silva@example.com");
        request.setPassword("securePassword123");
        request.setBatteryCapacityKwh(60.5);
        request.setFullRangeKm(300.0);

        assertEquals("Ana Silva", request.getName());
        assertEquals("ana.silva@example.com", request.getEmail());
        assertEquals("securePassword123", request.getPassword());
        assertEquals(60.5, request.getBatteryCapacityKwh());
        assertEquals(300.0, request.getFullRangeKm());
    }

    @Test
    void testAllArgsConstructorAndEquality() {
        SignUpRequest request1 = new SignUpRequest(
                "João Costa",
                "joao@example.com",
                "Password123",
                75.0,
                350.0
        );

        SignUpRequest request2 = new SignUpRequest(
                "João Costa",
                "joao@example.com",
                "Password123",
                75.0,
                350.0
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
}
