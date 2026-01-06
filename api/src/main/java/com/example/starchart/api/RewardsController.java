package com.example.starchart.api;

import com.example.starchart.domain.RewardRule;
import com.example.starchart.repo.ChildRepo;
import com.example.starchart.repo.RewardRuleRepo;
import com.example.starchart.repo.UserRepo;
import com.example.starchart.security.JwtPrincipal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardsController {

  private final UserRepo userRepo;
  private final ChildRepo childRepo;
  private final RewardRuleRepo rewardRepo;

  public RewardsController(UserRepo userRepo, ChildRepo childRepo, RewardRuleRepo rewardRepo) {
    this.userRepo = userRepo;
    this.childRepo = childRepo;
    this.rewardRepo = rewardRepo;
  }

  public record CreateRewardReq(
      UUID childId,              // nullable => family-level
      @Min(1) int threshold,
      @NotBlank String reward
  ) {}

  @PreAuthorize("hasRole('PARENT') or hasRole('SYSTEM_ADMIN')")
  @PostMapping
  public ResponseEntity<?> createReward(@AuthenticationPrincipal JwtPrincipal principal,
                                        @RequestBody CreateRewardReq req) {
    if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "User not found"));

    var user = userOpt.get();
    UUID famId = user.getFamilyAccountId();
    if (famId == null) return ResponseEntity.badRequest().body(Map.of("error", "Create a family first"));

    // If childId provided, enforce it belongs to this family
    if (req.childId() != null) {
      var childOpt = childRepo.findById(req.childId());
      if (childOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Child not found"));
      if (!famId.equals(childOpt.get().getFamilyAccountId())) {
        return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
      }
    }

    var rule = new RewardRule();
    rule.setFamilyAccountId(famId);
    rule.setChildId(req.childId());
    rule.setThreshold(req.threshold());
    rule.setReward(req.reward());
    rule.setActive(true);

    rewardRepo.save(rule);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", rule.getId().toString());
    out.put("childId", rule.getChildId() == null ? null : rule.getChildId().toString());
    out.put("threshold", rule.getThreshold());
    out.put("reward", rule.getReward());
    out.put("active", rule.isActive());
    return ResponseEntity.ok(out);
  }

  @PreAuthorize("hasRole('PARENT') or hasRole('VIEWER') or hasRole('SYSTEM_ADMIN')")
  @GetMapping
  public ResponseEntity<?> listRewards(@AuthenticationPrincipal JwtPrincipal principal,
                                       @RequestParam(required = false) UUID childId) {
    if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "User not found"));

    var user = userOpt.get();
    UUID famId = user.getFamilyAccountId();
    if (famId == null) return ResponseEntity.ok(Map.of("rewards", List.of()));

    List<RewardRule> rules;
    if (childId == null) {
      rules = rewardRepo.findAllByFamilyAccountIdAndActiveTrue(famId);
    } else {
      // allow child filter, but still enforce child belongs to same family
      var childOpt = childRepo.findById(childId);
      if (childOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Child not found"));
      if (!famId.equals(childOpt.get().getFamilyAccountId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

      rules = rewardRepo.findAllByFamilyAccountIdAndChildIdAndActiveTrue(famId, childId);
    }

    var payload = rules.stream().map(r -> {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", r.getId().toString());
      m.put("childId", r.getChildId() == null ? null : r.getChildId().toString());
      m.put("threshold", r.getThreshold());
      m.put("reward", r.getReward());
      m.put("active", r.isActive());
      return m;
    }).toList();

    // Map.of would NPE if any value null, so use LinkedHashMap
    Map<String, Object> resp = new LinkedHashMap<>();
    resp.put("rewards", payload);
    return ResponseEntity.ok(resp);
  }

  @PreAuthorize("hasRole('PARENT') or hasRole('SYSTEM_ADMIN')")
  @PostMapping("/{rewardId}/deactivate")
  public ResponseEntity<?> deactivate(@AuthenticationPrincipal JwtPrincipal principal,
                                      @PathVariable UUID rewardId) {
    if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    var userOpt = userRepo.findById(principal.userId());
    if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "User not found"));

    var user = userOpt.get();
    UUID famId = user.getFamilyAccountId();
    if (famId == null) return ResponseEntity.badRequest().body(Map.of("error", "Create a family first"));

    var ruleOpt = rewardRepo.findById(rewardId);
    if (ruleOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Reward not found"));
    var rule = ruleOpt.get();

    if (!famId.equals(rule.getFamilyAccountId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

    rule.setActive(false);
    rewardRepo.save(rule);

    return ResponseEntity.ok(Map.of("ok", true));
  }
}