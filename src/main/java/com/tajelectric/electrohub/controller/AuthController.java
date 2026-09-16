package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.User;
import com.tajelectric.electrohub.repository.UserRepository;
import com.tajelectric.electrohub.security.AuthUtil;
import com.tajelectric.electrohub.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public record RegisterRequest(
            @NotBlank String name,
            @Email String email,
            @Size(min = 6) String password,
            String phone) {}

    public record LoginRequest(@Email String email, @NotBlank String password) {}

    private Map<String, Object> userDto(User u) {
        return Map.of("id", u.getId(), "name", u.getName(),
                "email", u.getEmail(), "role", u.getRole());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByEmail(req.email().toLowerCase())) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        }
        User u = new User();
        u.setName(req.name());
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setPhone(req.phone());
        u.setRole("USER");
        userRepo.save(u);
        String token = jwtUtil.generateToken(u.getId(), u.getRole());
        return ResponseEntity.ok(Map.of("token", token, "user", userDto(u)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Optional<User> found = userRepo.findByEmail(req.email().toLowerCase());
        if (found.isEmpty() || !encoder.matches(req.password(), found.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        User u = found.get();
        String token = jwtUtil.generateToken(u.getId(), u.getRole());
        return ResponseEntity.ok(Map.of("token", token, "user", userDto(u)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Long userId = AuthUtil.currentUserId();
        java.util.HashMap<String, Object> body = new java.util.HashMap<>();
        if (userId == null) {
            body.put("user", null);
            return ResponseEntity.ok(body);
        }
        User u = userRepo.findById(userId).orElse(null);
        body.put("user", u == null ? null : userDto(u));
        return ResponseEntity.ok(body);
    }
}
