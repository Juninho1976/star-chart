package com.example.starchart.api;

import com.example.starchart.domain.FamilyAccount;
import com.example.starchart.repo.FamilyAccountRepo;
import com.example.starchart.repo.UserRepo;
import com.example.starchart.security.JwtPrincipal;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/family")
public class FamilyController {

  private final FamilyAccountRepo familyRepo;
  private final UserRepo userRepo;

  public FamilyController(FamilyAccountRepo familyRepo, UserRepo userRepo) {
    this.familyRepo = familyRepo;
    this.userRepo = userRepo;
  }

  public record CreateFamilyReq(@NotBlank String name) {}

  @PreAuthorize("hasRole('PARENT') or hasRole('SYSTEM_ADMIN')")
  @PostMapping
  public ResponseEntity<?> createFamily(@AuthenticationPrincipal JwtPrincipal principal,
                                        @RequestBody CreateFamilyReq req) {
    if (principal == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) {
      return ResponseEntity.status(401).body(Map.of("error", "User not found"));
    }

    var user = userOpt.get();

    if (user.getFamilyAccountId() != null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Family already exists for this user"));
    }

    var fam = new FamilyAccount();
    fam.setName(req.name());
    familyRepo.save(fam);

    user.setFamilyAccountId(fam.getId());
    userRepo.save(user);

    // Avoid Map.of with nulls (defensive)
    Map<String, Object> resp = new LinkedHashMap<>();
    resp.put("familyId", fam.getId().toString());
    resp.put("name", fam.getName() == null ? "" : fam.getName());

    return ResponseEntity.ok(resp);
  }

  @GetMapping
  public ResponseEntity<?> getMyFamily(@AuthenticationPrincipal JwtPrincipal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) {
      return ResponseEntity.status(401).body(Map.of("error", "User not found"));
    }

    var user = userOpt.get();
    var familyId = user.getFamilyAccountId();

    Map<String, Object> resp = new java.util.LinkedHashMap<>();

    if (familyId == null) {
      resp.put("family", null); // Map.of would NPE here
      return ResponseEntity.ok(resp);
    }

    var familyOpt = familyRepo.findById(familyId);
    if (familyOpt.isEmpty()) {
      resp.put("family", null); // still no 500
      return ResponseEntity.ok(resp);
    }

    var family = familyOpt.get();

    Map<String, Object> familyJson = new java.util.LinkedHashMap<>();
    familyJson.put("id", family.getId().toString());
    familyJson.put("name", family.getName() == null ? "" : family.getName());

    resp.put("family", familyJson);
    return ResponseEntity.ok(resp);
  }
}