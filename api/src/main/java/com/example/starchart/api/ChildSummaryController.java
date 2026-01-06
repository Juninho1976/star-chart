package com.example.starchart.api;

import com.example.starchart.repo.ChildRepo;
import com.example.starchart.repo.RewardRuleRepo;
import com.example.starchart.repo.StarEventRepo;
import com.example.starchart.repo.UserRepo;
import com.example.starchart.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/children/{childId}/summary")
public class ChildSummaryController {

  private final UserRepo userRepo;
  private final ChildRepo childRepo;
  private final StarEventRepo starRepo;
  private final RewardRuleRepo rewardRepo;

  public ChildSummaryController(UserRepo userRepo, ChildRepo childRepo, StarEventRepo starRepo, RewardRuleRepo rewardRepo) {
    this.userRepo = userRepo;
    this.childRepo = childRepo;
    this.starRepo = starRepo;
    this.rewardRepo = rewardRepo;
  }

  @PreAuthorize("hasRole('PARENT') or hasRole('VIEWER') or hasRole('SYSTEM_ADMIN')")
  @GetMapping
  public ResponseEntity<?> getSummary(@AuthenticationPrincipal JwtPrincipal principal,
                                      @PathVariable UUID childId) {
    if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "User not found"));

    var user = userOpt.get();
    UUID famId = user.getFamilyAccountId();
    if (famId == null) {
      Map<String, Object> resp = new LinkedHashMap<>();
      resp.put("totalStars", 0);
      resp.put("nextReward", null);
      return ResponseEntity.ok(resp);
    }

    var childOpt = childRepo.findById(childId);
    if (childOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Child not found"));
    var child = childOpt.get();
    if (!famId.equals(child.getFamilyAccountId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

    int total = starRepo.sumStarsForChild(childId);

    // Prefer child-specific rules; if none, fall back to family-level
    var childRules = rewardRepo.findAllByFamilyAccountIdAndChildIdAndActiveTrue(famId, childId);
    var rules = !childRules.isEmpty()
        ? childRules
        : rewardRepo.findAllByFamilyAccountIdAndActiveTrue(famId).stream()
            .filter(r -> r.getChildId() == null)
            .toList();

    var next = rules.stream()
        .filter(r -> r.getThreshold() > total)
        .min(Comparator.comparingInt(r -> r.getThreshold()))
        .orElse(null);

    Map<String, Object> resp = new LinkedHashMap<>();
    resp.put("childId", childId.toString());
    resp.put("totalStars", total);

    if (next == null) {
      resp.put("nextReward", null);
    } else {
      Map<String, Object> nr = new LinkedHashMap<>();
      nr.put("threshold", next.getThreshold());
      nr.put("reward", next.getReward());
      nr.put("starsRemaining", next.getThreshold() - total);
      resp.put("nextReward", nr);
    }

    return ResponseEntity.ok(resp);
  }
}