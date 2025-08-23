package com.solsolhey.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.solsolhey.ranking.entity.ContestEntry;

/**
 * 콘테스트 엔트리 레포지토리
 */
@Repository
public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {

    /**
     * 콘테스트 타입별 엔트리 조회 (투표수 내림차순)
     */
    Page<ContestEntry> findByContestTypeAndStatusOrderByVotesDesc(
            ContestEntry.ContestType contestType, 
            ContestEntry.EntryStatus status,
            Pageable pageable);

    /**
     * 콘테스트 타입별 엔트리 조회 (트렌드 점수 내림차순)
     */
    Page<ContestEntry> findByContestTypeAndStatusOrderByTrendScoreDesc(
            ContestEntry.ContestType contestType,
            ContestEntry.EntryStatus status,
            Pageable pageable);

    /**
     * 콘테스트 타입별 엔트리 조회 (최신순)
     */
    Page<ContestEntry> findByContestTypeAndStatusOrderByCreatedAtDesc(
            ContestEntry.ContestType contestType,
            ContestEntry.EntryStatus status,
            Pageable pageable);

    /**
     * 캠퍼스별 교내 랭킹 조회 (투표수 내림차순)
     */
    @Query("SELECT ce FROM ContestEntry ce " +
           "JOIN User u ON ce.userId = u.userId " +
           "WHERE ce.contestType = :contestType " +
           "AND ce.status = :status " +
           "AND u.campus = :campus " +
           "ORDER BY ce.votes DESC")
    Page<ContestEntry> findCampusRankingByVotes(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("status") ContestEntry.EntryStatus status,
            @Param("campus") String campus,
            Pageable pageable);

    /**
     * 캠퍼스별 교내 랭킹 조회 (트렌드 점수 내림차순)
     */
    @Query("SELECT ce FROM ContestEntry ce " +
           "JOIN User u ON ce.userId = u.userId " +
           "WHERE ce.contestType = :contestType " +
           "AND ce.status = :status " +
           "AND u.campus = :campus " +
           "ORDER BY ce.trendScore DESC")
    Page<ContestEntry> findCampusRankingByTrend(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("status") ContestEntry.EntryStatus status,
            @Param("campus") String campus,
            Pageable pageable);

    /**
     * 캠퍼스별 교내 랭킹 조회 (최신순)
     */
    @Query("SELECT ce FROM ContestEntry ce " +
           "JOIN User u ON ce.userId = u.userId " +
           "WHERE ce.contestType = :contestType " +
           "AND ce.status = :status " +
           "AND u.campus = :campus " +
           "ORDER BY ce.createdAt DESC")
    Page<ContestEntry> findCampusRankingByNewest(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("status") ContestEntry.EntryStatus status,
            @Param("campus") String campus,
            Pageable pageable);

    /**
     * 특정 학교별 전국 랭킹 조회 (투표수 내림차순)
     */
    @Query("SELECT ce FROM ContestEntry ce " +
           "JOIN User u ON ce.userId = u.userId " +
           "WHERE ce.contestType = :contestType " +
           "AND ce.status = :status " +
           "AND u.campus = :school " +
           "ORDER BY ce.votes DESC")
    Page<ContestEntry> findNationalRankingBySchool(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("status") ContestEntry.EntryStatus status,
            @Param("school") String school,
            Pageable pageable);

    /**
     * 사용자별 엔트리 조회
     */
    List<ContestEntry> findByUserIdAndContestType(Long userId, ContestEntry.ContestType contestType);

    /**
     * 마스코트별 엔트리 조회
     */
    List<ContestEntry> findByMascotIdAndContestType(Long mascotId, ContestEntry.ContestType contestType);

    /**
     * 중복 참가 체크
     */
    boolean existsByUserIdAndMascotIdAndContestTypeAndStatus(
            Long userId, Long mascotId, ContestEntry.ContestType contestType, ContestEntry.EntryStatus status);

    /**
     * 콘테스트별 엔트리 조회
     */
    List<ContestEntry> findByContestIdAndStatus(Long contestId, ContestEntry.EntryStatus status);

    /**
     * 전체 엔트리 수 조회 (콘테스트 타입별)
     */
    long countByContestTypeAndStatus(ContestEntry.ContestType contestType, ContestEntry.EntryStatus status);

    /**
     * 캠퍼스별 엔트리 수 조회
     */
    @Query("SELECT COUNT(ce) FROM ContestEntry ce " +
           "JOIN User u ON ce.userId = u.userId " +
           "WHERE ce.contestType = :contestType " +
           "AND ce.status = :status " +
           "AND u.campus = :campus")
    long countCampusEntries(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("status") ContestEntry.EntryStatus status,
            @Param("campus") String campus);

    /**
     * 기간별 엔트리 조회 (생성일 기준)
     */
    Page<ContestEntry> findByContestTypeAndStatusAndCreatedAtBetween(
            ContestEntry.ContestType contestType,
            ContestEntry.EntryStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * 인기 엔트리 조회 (최소 투표수 이상)
     */
    @Query("SELECT ce FROM ContestEntry ce " +
           "WHERE ce.contestType = :contestType " +
           "AND ce.status = :status " +
           "AND ce.votes >= :minVotes " +
           "ORDER BY ce.votes DESC")
    Page<ContestEntry> findPopularEntries(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("status") ContestEntry.EntryStatus status,
            @Param("minVotes") Integer minVotes,
            Pageable pageable);
}
