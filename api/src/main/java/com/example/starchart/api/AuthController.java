package com.example.starchart.api;

import com.example.starchart.domain.Role;
import com.example.starchart.domain.User;
import com.example.starchart.repo.UserRepo;
import com.example.starchart.security.JwtService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final UserRepo userRepo;
  private final JwtService jwtService;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public AuthController(UserRepo userRepo, JwtService jwtService) {
    this.userRepo = userRepo;
    this.jwtService = jwtService;
  }

  public record RegisterReq(@Email @NotBlank String email, @NotBlank String password) {}
  public record LoginReq(@Email @NotBlank String email, @NotBlank String password) {}

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterReq req) {
    if (userRepo.findByEmailIgnoreCase(req.email()).isPresent()) {
      return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
    }
    var u = new User();
    u.setEmail(req.email().toLowerCase());
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole(Role.PARENT); // Option A: sign-ups create parent accounts
    userRepo.save(u);

    String token = jwtService.issueToken(u.getId(), u.getEmail(), u.getRole());
    return ResponseEntity.ok(Map.of("accessToken", token));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginReq req) {
    var u = userRepo.findByEmailIgnoreCase(req.email())
        .orElse(null);
    if (u == null || !encoder.matches(req.password(), u.getPasswordHash())) {
      return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
    String token = jwtService.issueToken(u.getId(), u.getEmail(), u.getRole());
    return ResponseEntity.ok(Map.of("accessToken", token));
  }
}