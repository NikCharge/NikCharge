package tqs.backend.model;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.UserRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Client client = new Client();
        client.setId(1L);
        client.setName("Alice");
        client.setEmail("alice@example.com");
        client.setPasswordHash("hashedpass");
        client.setRole(UserRole.CLIENT);
        client.setBatteryCapacityKwh(60.0);
        client.setFullRangeKm(400.0);

        assertEquals(1L, client.getId());
        assertEquals("Alice", client.getName());
        assertEquals("alice@example.com", client.getEmail());
        assertEquals("hashedpass", client.getPasswordHash());
        assertEquals(UserRole.CLIENT, client.getRole());
        assertEquals(60.0, client.getBatteryCapacityKwh());
        assertEquals(400.0, client.getFullRangeKm());
        assertNotNull(client.getReservations());
    }

    @Test
    void testAllArgsConstructor() {
        Client client = new Client(
                2L,
                "Bob",
                "bob@example.com",
                "pass",
                UserRole.CLIENT,
                75.0,
                500.0,
                List.of()
        );

        assertEquals("Bob", client.getName());
        assertEquals("bob@example.com", client.getEmail());
        assertEquals(75.0, client.getBatteryCapacityKwh());
    }

    @Test
    void testBuilder() {
        Client client = Client.builder()
                .name("Carol")
                .email("carol@example.com")
                .passwordHash("hashed")
                .role(UserRole.CLIENT)
                .batteryCapacityKwh(85.0)
                .fullRangeKm(600.0)
                .build();

        assertEquals("Carol", client.getName());
        assertEquals("carol@example.com", client.getEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        Client client1 = Client.builder()
                .email("same@example.com")
                .build();

        Client client2 = Client.builder()
                .email("same@example.com")
                .build();

        Client client3 = Client.builder()
                .email("diff@example.com")
                .build();

        assertEquals(client1, client2); // same email
        assertEquals(client1.hashCode(), client2.hashCode());

        assertNotEquals(client1, client3); // different email
    }

    @Test
    void testToString() {
        Client client = Client.builder()
                .id(5L)
                .name("Diana")
                .email("diana@example.com")
                .role(UserRole.CLIENT)
                .batteryCapacityKwh(90.0)
                .fullRangeKm(550.0)
                .build();

        String toString = client.toString();

        assertTrue(toString.contains("Diana"));
        assertTrue(toString.contains("diana@example.com"));
        assertTrue(toString.contains("90.0"));
    }
}
