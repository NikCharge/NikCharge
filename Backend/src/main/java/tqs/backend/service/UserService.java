package tqs.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tqs.backend.model.User;
import tqs.backend.model.enums.UserRole;
import tqs.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User signUp(String name, String email, String rawPassword, Double batteryCapacityKwh, Double fullRangeKm) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(UserRole.USER)
                .batteryCapacityKwh(batteryCapacityKwh)
                .fullRangeKm(fullRangeKm)
                .build();

        return userRepository.save(user);
    }
}
