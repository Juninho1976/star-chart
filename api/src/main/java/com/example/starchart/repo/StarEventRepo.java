package com.example.starchart.repo;

import com.example.starchart.domain.StarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StarEventRepo extends JpaRepository<StarEvent, UUID> {

  List<StarEvent> findTop50ByChildIdOrderByCreatedAtDesc(UUID childId);

  @Query("select coalesce(sum(e.delta), 0) from StarEvent e where e.childId = :childId")
  int sumStarsForChild(UUID childId);
}