package com.solsolhey.mascot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solsolhey.mascot.domain.MascotSnapshot;

public interface MascotSnapshotRepository extends JpaRepository<MascotSnapshot, Long> {
    List<MascotSnapshot> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserId(Long userId);
    List<MascotSnapshot> findByUserIdOrderByCreatedAtAsc(Long userId);
}

