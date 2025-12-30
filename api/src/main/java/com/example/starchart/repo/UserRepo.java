package com.example.starchart.repo;

import com.example.starchart.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
  Optional<User> findByEmailIgnoreCase(String email);
}