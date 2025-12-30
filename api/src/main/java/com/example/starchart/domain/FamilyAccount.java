package com.example.starchart.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "family_accounts")
public class FamilyAccount {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  public UUID getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}