package com.example.starchart.repo;

import com.example.starchart.domain.FamilyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FamilyAccountRepo extends JpaRepository<FamilyAccount, UUID> {}