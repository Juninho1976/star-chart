package com.example.starchart.repo;

import com.example.starchart.domain.Role;
import com.example.starchart.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;


public interface UserRepo extends JpaRepository<User, UUID> {
  Optional<User> findByEmailIgnoreCase(String email);
  // Optional<User> findByEmail(String email); - removed this as like the one for ignore case ^
  List<User> findAllByFamilyAccountIdAndRole(UUID familyAccountId, Role role);
}