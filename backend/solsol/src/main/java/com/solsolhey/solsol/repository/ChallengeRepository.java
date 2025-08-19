package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.Challenge;
import com.solsolhey.solsol.entity.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 챌린지 레포지토리
 */
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    /**
     * 활성화된 챌린지 목록 조회
     */
    List<Challenge> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 카테고리별 활성화된 챌린지 목록 조회
     */
    List<Challenge> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(ChallengeCategory category);

    /**
     * 챌린지 타입별 활성화된 챌린지 목록 조회
     */
    @Query("SELECT c FROM Challenge c WHERE c.challengeType = :challengeType AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Challenge> findByTypeAndActiveTrue(@Param("challengeType") Challenge.ChallengeType challengeType);

    /**
     * 현재 진행 가능한 챌린지 목록 조회
     */
    @Query("SELECT c FROM Challenge c WHERE c.isActive = true AND " +
           "(c.startDate IS NULL OR c.startDate <= :now) AND " +
           "(c.endDate IS NULL OR c.endDate > :now) " +
           "ORDER BY c.createdAt DESC")
    List<Challenge> findAvailableChallenges(@Param("now") LocalDateTime now);

    /**
     * 카테고리별 진행 가능한 챌린지 목록 조회
     */
    @Query("SELECT c FROM Challenge c WHERE c.category = :category AND c.isActive = true AND " +
           "(c.startDate IS NULL OR c.startDate <= :now) AND " +
           "(c.endDate IS NULL OR c.endDate > :now) " +
           "ORDER BY c.createdAt DESC")
    List<Challenge> findAvailableChallengesByCategory(@Param("category") ChallengeCategory category, 
                                                      @Param("now") LocalDateTime now);

    /**
     * 특정 기간에 시작된 챌린지 조회
     */
    @Query("SELECT c FROM Challenge c WHERE c.startDate >= :startDate AND c.startDate <= :endDate " +
           "ORDER BY c.startDate ASC")
    List<Challenge> findByStartDateBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 만료된 챌린지 조회
     */
    @Query("SELECT c FROM Challenge c WHERE c.endDate < :now AND c.isActive = true")
    List<Challenge> findExpiredChallenges(@Param("now") LocalDateTime now);
}
