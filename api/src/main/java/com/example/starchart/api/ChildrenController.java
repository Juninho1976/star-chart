package com.example.starchart.api;

import com.example.starchart.domain.Child;
import com.example.starchart.repo.ChildRepo;
import com.example.starchart.repo.UserRepo;
import com.example.starchart.security.JwtPrincipal;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/children")
public class ChildrenController {

  private final ChildRepo childRepo;
  private final UserRepo userRepo;

  public ChildrenController(ChildRepo childRepo, UserRepo userRepo) {
    this.childRepo = childRepo;
    this.userRepo = userRepo;
  }

  public record CreateChildReq(@NotBlank String name) {}

  @PreAuthorize("hasRole('PARENT')")
  @PostMapping
  public ResponseEntity<?> createChild(@AuthenticationPrincipal JwtPrincipal principal,
                                      @RequestBody CreateChildReq req) {
    var user = userRepo.findById(principal.userId()).orElseThrow();
    UUID famId = user.getFamilyAccountId();
    if (famId == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Create a family first"));
    }

    var c = new Child();
    c.setFamilyAccountId(famId);
    c.setName(req.name());
    childRepo.save(c);

    return ResponseEntity.ok(Map.of("id", c.getId(), "name", c.getName()));
  }

  @GetMapping
  public ResponseEntity<?> listChildren(@AuthenticationPrincipal JwtPrincipal principal) {
    var user = userRepo.findById(principal.userId()).orElseThrow();
    UUID famId = user.getFamilyAccountId();
    if (famId == null) return ResponseEntity.ok(Map.of("children", List.of()));

    var children = childRepo.findAllByFamilyAccountId(famId)
        .stream().map(c -> Map.of("id", c.getId(), "name", c.getName()))
        .toList();

    return ResponseEntity.ok(Map.of("children", children));
  }
}