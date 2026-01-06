package com.example.starchart.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reward_rule")
public class RewardRule {

  @Id
  private UUID id;

  @Column(name = "family_account_id", nullable = false)
  private UUID familyAccountId;

  @Column(name = "child_id")
  private UUID childId; // nullable = family-level rule

  @Column(name = "threshold", nullable = false)
  private int threshold;

  @Column(name = "reward", nullable = false)
  private String reward;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getFamilyAccountId() { return familyAccountId; }
  public UUID getChildId() { return childId; }
  public int getThreshold() { return threshold; }
  public String getReward() { return reward; }
  public boolean isActive() { return active; }
  public Instant getCreatedAt() { return createdAt; }

  public void setId(UUID id) { this.id = id; }
  public void setFamilyAccountId(UUID familyAccountId) { this.familyAccountId = familyAccountId; }
  public void setChildId(UUID childId) { this.childId = childId; }
  public void setThreshold(int threshold) { this.threshold = threshold; }
  public void setReward(String reward) { this.reward = reward; }
  public void setActive(boolean active) { this.active = active; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}