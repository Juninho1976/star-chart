package com.example.starchart.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "star_event")
public class StarEvent {

  @Id
  private UUID id;

  @Column(name = "family_account_id", nullable = false)
  private UUID familyAccountId;

  @Column(name = "child_id", nullable = false)
  private UUID childId;

  @Column(name = "created_by_user_id", nullable = false)
  private UUID createdByUserId;

  @Column(name = "delta", nullable = false)
  private int delta;

  @Column(name = "reason")
  private String reason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = Instant.now();
  }

  // getters/setters
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getFamilyAccountId() { return familyAccountId; }
  public void setFamilyAccountId(UUID familyAccountId) { this.familyAccountId = familyAccountId; }

  public UUID getChildId() { return childId; }
  public void setChildId(UUID childId) { this.childId = childId; }

  public UUID getCreatedByUserId() { return createdByUserId; }
  public void setCreatedByUserId(UUID createdByUserId) { this.createdByUserId = createdByUserId; }

  public int getDelta() { return delta; }
  public void setDelta(int delta) { this.delta = delta; }

  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}