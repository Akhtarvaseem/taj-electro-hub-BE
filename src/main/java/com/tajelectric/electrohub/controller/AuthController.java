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
        java.util.HashMap<String, Object> m = new java.util.HashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        m.put("phone", u.getPhone());
        m.put("avatar", u.getAvatar());
        return m;
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

    /** Update name, phone and/or profile picture (avatar URL or data-URI). */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User u = userRepo.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("error", "Not found"));

        if (body.get("name") != null && !body.get("name").toString().isBlank())
            u.setName(body.get("name").toString().trim());
        if (body.containsKey("phone"))
            u.setPhone(body.get("phone") == null ? null : body.get("phone").toString());
        if (body.containsKey("avatar")) {
            String av = body.get("avatar") == null ? null : body.get("avatar").toString();
            if (av != null && av.length() > 800_000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Image is too large. Please use a smaller photo."));
            }
            u.setAvatar(av);
        }
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("user", userDto(u)));
    }
}
