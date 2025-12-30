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

import java.util.Map;
import java.util.UUID;

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
    var user = userRepo.findById(principal.userId()).orElseThrow();

    if (user.getFamilyAccountId() != null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Family already exists for this user"));
    }

    var fam = new FamilyAccount();
    fam.setName(req.name());
    familyRepo.save(fam);

    user.setFamilyAccountId(fam.getId());
    userRepo.save(user);

    return ResponseEntity.ok(Map.of("familyId", fam.getId(), "name", fam.getName()));
  }

  @GetMapping
  public ResponseEntity<?> getMyFamily(@AuthenticationPrincipal JwtPrincipal principal) {
    var user = userRepo.findById(principal.userId()).orElseThrow();
    UUID famId = user.getFamilyAccountId();
    if (famId == null) return ResponseEntity.ok(Map.of("family", null));
    var fam = familyRepo.findById(famId).orElse(null);
    return ResponseEntity.ok(Map.of("family", fam == null ? null : Map.of("id", fam.getId(), "name", fam.getName())));
  }
}