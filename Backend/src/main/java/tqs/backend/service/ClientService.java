package tqs.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tqs.backend.model.Client;
import tqs.backend.model.enums.UserRole;
import tqs.backend.repository.ClientRepository;
import tqs.backend.dto.SignUpRequest;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Client signUp(SignUpRequest request) {
        String email = request.getEmail();
        if (clientRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        Client client = new Client();
        client.setName(request.getName());
        client.setEmail(email);
        client.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        client.setBatteryCapacityKwh(request.getBatteryCapacityKwh());
        client.setFullRangeKm(request.getFullRangeKm());
        client.setRole(UserRole.CLIENT);

        return clientRepository.save(client);
    }
}
