package tqs.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.model.User;
import tqs.backend.service.UserService;
import tqs.backend.dto.SignUpRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@RequestBody SignUpRequest request) {
        User user = userService.signUp(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getBatteryCapacityKwh(),
                request.getFullRangeKm()
        );
        return ResponseEntity.ok(user);
    }
}
