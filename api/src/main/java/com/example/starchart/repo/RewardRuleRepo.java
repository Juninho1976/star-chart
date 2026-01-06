package com.example.starchart.repo;

import com.example.starchart.domain.RewardRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardRuleRepo extends JpaRepository<RewardRule, UUID> {

  List<RewardRule> findAllByFamilyAccountIdAndActiveTrue(UUID familyAccountId);

  List<RewardRule> findAllByFamilyAccountIdAndChildIdAndActiveTrue(UUID familyAccountId, UUID childId);
}