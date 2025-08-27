package com.solsolhey.mascot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.solsolhey.mascot.domain.MascotSnapshot;

public interface MascotSnapshotRepository extends JpaRepository<MascotSnapshot, Long> {
    List<MascotSnapshot> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserId(Long userId);
    List<MascotSnapshot> findByUserIdOrderByCreatedAtAsc(Long userId);
    
    /**
     * 여러 마스코트 ID에 대한 스냅샷을 일괄 조회
     */
    @Query("SELECT ms FROM MascotSnapshot ms WHERE ms.mascotId IN :mascotIds")
    List<MascotSnapshot> findByMascotIdIn(@Param("mascotIds") List<Long> mascotIds);

    /**
     * 특정 마스코트 ID의 스냅샷을 생성일 기준 내림차순으로 조회
     */
    @Query("SELECT ms FROM MascotSnapshot ms WHERE ms.mascotId = :mascotId ORDER BY ms.createdAt DESC")
    List<MascotSnapshot> findByMascotIdOrderByCreatedAtDesc(@Param("mascotId") Long mascotId);
}

