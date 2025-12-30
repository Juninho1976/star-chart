package com.example.starchart.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "children")
public class Child {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "family_account_id", nullable = false)
  private UUID familyAccountId;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  public UUID getId() { return id; }
  public UUID getFamilyAccountId() { return familyAccountId; }
  public void setFamilyAccountId(UUID familyAccountId) { this.familyAccountId = familyAccountId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}