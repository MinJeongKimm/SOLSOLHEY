package com.solsolhey.ranking.repository;

import com.solsolhey.ranking.entity.ContestEntry;
import com.solsolhey.ranking.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 투표 레포지토리
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * 중복 투표 체크 (투표자 + 엔트리)
     */
    boolean existsByVoterIdAndEntryId(Long voterId, Long entryId);

    /**
     * 중복 투표 체크 (투표자 + 엔트리 + 콘테스트 타입)
     */
    boolean existsByVoterIdAndEntryIdAndContestType(
            Long voterId, Long entryId, ContestEntry.ContestType contestType);

    /**
     * 멱등키 중복 체크
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * 멱등키로 투표 조회
     */
    Optional<Vote> findByIdempotencyKey(String idempotencyKey);

    /**
     * 엔트리별 투표 목록 조회
     */
    List<Vote> findByEntryIdOrderByCreatedAtDesc(Long entryId);

    /**
     * 투표자별 투표 목록 조회
     */
    List<Vote> findByVoterIdOrderByCreatedAtDesc(Long voterId);

    /**
     * 엔트리별 총 투표 수 조회
     */
    @Query("SELECT SUM(v.weight) FROM Vote v WHERE v.entryId = :entryId")
    Long sumVotesByEntryId(@Param("entryId") Long entryId);

    /**
     * 콘테스트 타입별 총 투표 수 조회
     */
    @Query("SELECT SUM(v.weight) FROM Vote v WHERE v.contestType = :contestType")
    Long sumVotesByContestType(@Param("contestType") ContestEntry.ContestType contestType);

    /**
     * 투표자의 일일 투표 수 조회
     */
    @Query("SELECT COUNT(v) FROM Vote v " +
           "WHERE v.voterId = :voterId " +
           "AND v.contestType = :contestType " +
           "AND DATE(v.createdAt) = DATE(:date)")
    Long countDailyVotesByVoter(
            @Param("voterId") Long voterId,
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("date") LocalDateTime date);

    /**
     * 투표자의 특정 콘테스트 투표 수 조회
     */
    long countByVoterIdAndContestId(Long voterId, Long contestId);

    /**
     * 캠퍼스별 투표 수 조회
     */
    long countByCampusIdAndContestType(Long campusId, ContestEntry.ContestType contestType);

    /**
     * 기간별 투표 조회
     */
    Page<Vote> findByContestTypeAndCreatedAtBetween(
            ContestEntry.ContestType contestType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * 엔트리별 기간 투표 수 조회
     */
    @Query("SELECT SUM(v.weight) FROM Vote v " +
           "WHERE v.entryId = :entryId " +
           "AND v.createdAt BETWEEN :startDate AND :endDate")
    Long sumVotesByEntryIdAndPeriod(
            @Param("entryId") Long entryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 투표자의 최근 투표 조회
     */
    Optional<Vote> findTopByVoterIdAndContestTypeOrderByCreatedAtDesc(
            Long voterId, ContestEntry.ContestType contestType);

    /**
     * 엔트리별 최근 투표 조회
     */
    Optional<Vote> findTopByEntryIdOrderByCreatedAtDesc(Long entryId);

    /**
     * 활발한 투표자 조회 (기간별)
     */
    @Query("SELECT v.voterId, COUNT(v) as voteCount FROM Vote v " +
           "WHERE v.contestType = :contestType " +
           "AND v.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY v.voterId " +
           "ORDER BY voteCount DESC")
    Page<Object[]> findActiveVoters(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 인기 엔트리 조회 (기간별 투표수 기준)
     */
    @Query("SELECT v.entryId, SUM(v.weight) as totalVotes FROM Vote v " +
           "WHERE v.contestType = :contestType " +
           "AND v.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY v.entryId " +
           "ORDER BY totalVotes DESC")
    Page<Object[]> findPopularEntriesByPeriod(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 투표 트렌드 분석 (시간대별)
     */
    @Query("SELECT HOUR(v.createdAt) as hour, COUNT(v) as voteCount FROM Vote v " +
           "WHERE v.contestType = :contestType " +
           "AND DATE(v.createdAt) = DATE(:date) " +
           "GROUP BY HOUR(v.createdAt) " +
           "ORDER BY hour")
    List<Object[]> findVoteTrendsByHour(
            @Param("contestType") ContestEntry.ContestType contestType,
            @Param("date") LocalDateTime date);
}
