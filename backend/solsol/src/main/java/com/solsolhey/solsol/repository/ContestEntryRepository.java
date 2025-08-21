package com.solsolhey.solsol.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.solsolhey.solsol.entity.ContestEntry;

@Repository
public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {

    /**
     * 특정 콘테스트 타입의 모든 참가자 조회 (점수 순으로 정렬)
     */
    @Query("SELECT ce FROM ContestEntry ce WHERE ce.contestType = :contestType AND ce.status = 'ACTIVE' ORDER BY ce.score DESC")
    List<ContestEntry> findByContestTypeOrderByScoreDesc(@Param("contestType") ContestEntry.ContestType contestType);

    /**
     * 특정 콘테스트 타입의 참가자들을 페이징으로 조회 (점수 순으로 정렬)
     */
    @Query("SELECT ce FROM ContestEntry ce WHERE ce.contestType = :contestType AND ce.status = 'ACTIVE' ORDER BY ce.score DESC")
    Page<ContestEntry> findByContestTypeOrderByScoreDesc(@Param("contestType") ContestEntry.ContestType contestType, Pageable pageable);

    /**
     * 특정 사용자의 콘테스트 참가 내역 조회
     */
    List<ContestEntry> findByUserIdAndContestType(Long userId, ContestEntry.ContestType contestType);

    /**
     * 특정 사용자가 특정 콘테스트에 참가했는지 확인
     */
    boolean existsByUserIdAndMascotIdAndContestType(Long userId, Long mascotId, ContestEntry.ContestType contestType);

    /**
     * 특정 마스코트의 콘테스트 참가 내역 조회
     */
    List<ContestEntry> findByMascotIdAndContestType(Long mascotId, ContestEntry.ContestType contestType);
}