package tqs.backend.model;

import org.junit.jupiter.api.Test;
import tqs.backend.model.enums.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    @Test
    void testEqualsAndHashCode() {
        Client a = new Client(1L, "Alice", "alice@mail.com", "pass", UserRole.CLIENT, 70.0, 350.0);
        Client b = new Client(1L, "Alice", "alice@mail.com", "pass", UserRole.CLIENT, 70.0, 350.0);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

}
