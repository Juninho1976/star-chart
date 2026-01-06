package com.example.starchart.api;

import com.example.starchart.domain.StarEvent;
import com.example.starchart.repo.ChildRepo;
import com.example.starchart.repo.StarEventRepo;
import com.example.starchart.repo.UserRepo;
import com.example.starchart.security.JwtPrincipal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/children/{childId}/stars")
public class StarEventsController {

  private final UserRepo userRepo;
  private final ChildRepo childRepo;
  private final StarEventRepo starEventRepo;

  public StarEventsController(UserRepo userRepo, ChildRepo childRepo, StarEventRepo starEventRepo) {
    this.userRepo = userRepo;
    this.childRepo = childRepo;
    this.starEventRepo = starEventRepo;
  }

  public record StarDeltaReq(
      @Min(-1) @Max(1) int delta,
      String reason
  ) {}

  @PreAuthorize("hasRole('PARENT') or hasRole('SYSTEM_ADMIN')")
  @PostMapping
  public ResponseEntity<?> addStar(@AuthenticationPrincipal JwtPrincipal principal,
                                  @PathVariable UUID childId,
                                  @RequestBody StarDeltaReq req) {
    if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "User not found"));
    var user = userOpt.get();

    var famId = user.getFamilyAccountId();
    if (famId == null) return ResponseEntity.badRequest().body(Map.of("error", "Create a family first"));

    if (req.delta() != 1 && req.delta() != -1) {
      return ResponseEntity.badRequest().body(Map.of("error", "delta must be 1 or -1"));
    }

    var childOpt = childRepo.findById(childId);
    if (childOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Child not found"));

    var child = childOpt.get();
    if (!famId.equals(child.getFamilyAccountId())) {
      return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
    }

    var e = new StarEvent();
    e.setFamilyAccountId(famId);
    e.setChildId(childId);
    e.setCreatedByUserId(user.getId());
    e.setDelta(req.delta());
    e.setReason(req.reason());

    starEventRepo.save(e);

    int total = starEventRepo.sumStarsForChild(childId);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("eventId", e.getId().toString());
    out.put("childId", childId.toString());
    out.put("delta", e.getDelta());
    out.put("totalStars", total);
    out.put("createdAt", e.getCreatedAt().toString());
    out.put("reason", e.getReason() == null ? "" : e.getReason());
    return ResponseEntity.ok(out);
  }

  @PreAuthorize("hasRole('PARENT') or hasRole('VIEWER') or hasRole('SYSTEM_ADMIN')")
  @GetMapping
  public ResponseEntity<?> getStars(@AuthenticationPrincipal JwtPrincipal principal,
                                   @PathVariable UUID childId) {
    if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "User not found"));
    var user = userOpt.get();

    var famId = user.getFamilyAccountId();
    if (famId == null) {
      Map<String, Object> resp = new LinkedHashMap<>();
      resp.put("totalStars", 0);
      resp.put("events", java.util.List.of());
      return ResponseEntity.ok(resp);
    }

    var childOpt = childRepo.findById(childId);
    if (childOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Child not found"));

    var child = childOpt.get();
    if (!famId.equals(child.getFamilyAccountId())) {
      return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
    }

    int total = starEventRepo.sumStarsForChild(childId);
    var events = starEventRepo.findTop50ByChildIdOrderByCreatedAtDesc(childId).stream().map(e -> {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", e.getId().toString());
      m.put("delta", e.getDelta());
      m.put("reason", e.getReason() == null ? "" : e.getReason());
      m.put("createdAt", e.getCreatedAt().toString());
      return m;
    }).toList();

    Map<String, Object> resp = new LinkedHashMap<>();
    resp.put("childId", childId.toString());
    resp.put("totalStars", total);
    resp.put("events", events);
    return ResponseEntity.ok(resp);
  }
}