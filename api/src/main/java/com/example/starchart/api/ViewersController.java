package com.example.starchart.api;

import com.example.starchart.domain.Role;
import com.example.starchart.domain.User;
import com.example.starchart.repo.UserRepo;
import com.example.starchart.security.JwtPrincipal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/viewers")
public class ViewersController {

  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;

  public ViewersController(UserRepo userRepo, PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  public record CreateViewerReq(
      @Email @NotBlank String email,
      @NotBlank String password
  ) {}

  /**
   * Create a VIEWER user in the same family as the authenticated parent.
   */
  @PreAuthorize("hasRole('PARENT') or hasRole('SYSTEM_ADMIN')")
  @PostMapping
  public ResponseEntity<?> createViewer(@AuthenticationPrincipal JwtPrincipal principal,
                                        @RequestBody CreateViewerReq req) {
    if (principal == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    var parentOpt = userRepo.findById(principal.userId());
    if (parentOpt.isEmpty()) {
      return ResponseEntity.status(401).body(Map.of("error", "User not found"));
    }

    var parent = parentOpt.get();
    UUID famId = parent.getFamilyAccountId();
    if (famId == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Create a family first"));
    }

    if (userRepo.findByEmailIgnoreCase(req.email()).isPresent()) {
      return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
    }

    var viewer = new User();
    viewer.setEmail(req.email().trim().toLowerCase());
    viewer.setPasswordHash(passwordEncoder.encode(req.password()));
    viewer.setRole(Role.VIEWER);
    viewer.setFamilyAccountId(famId);

    userRepo.save(viewer);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", viewer.getId().toString());
    out.put("email", viewer.getEmail());
    out.put("role", viewer.getRole().name());
    out.put("familyAccountId", famId.toString());

    return ResponseEntity.ok(out);
  }

  /**
   * List VIEWER users in the same family as the authenticated parent.
   */
  @PreAuthorize("hasRole('PARENT') or hasRole('SYSTEM_ADMIN')")
  @GetMapping
  public ResponseEntity<?> listViewers(@AuthenticationPrincipal JwtPrincipal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    var parentOpt = userRepo.findById(principal.userId());
    if (parentOpt.isEmpty()) {
      return ResponseEntity.status(401).body(Map.of("error", "User not found"));
    }

    UUID famId = parentOpt.get().getFamilyAccountId();
    if (famId == null) {
      return ResponseEntity.ok(Map.of("viewers", List.of()));
    }

    var viewers = userRepo.findAllByFamilyAccountIdAndRole(famId, Role.VIEWER).stream().map(u -> {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", u.getId().toString());
      m.put("email", u.getEmail());
      return m;
    }).toList();

    return ResponseEntity.ok(Map.of("viewers", viewers));
  }
}