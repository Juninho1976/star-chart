package com.example.starchart.repo;

import com.example.starchart.domain.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChildRepo extends JpaRepository<Child, UUID> {
  List<Child> findAllByFamilyAccountId(UUID familyAccountId);
}