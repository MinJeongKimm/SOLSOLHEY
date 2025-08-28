package com.solsolhey.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.solsolhey.ranking.entity.Vote;

/**
 * 투표 레포지토리 (마스코트 기반)
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * 중복 투표 체크 (투표자 + 마스코트)
     */
    boolean existsByVoterIdAndMascotId(Long voterId, Long mascotId);

    /**
     * 중복 투표 체크 (투표자 + 마스코트 + 투표 타입)
     */
    boolean existsByVoterIdAndMascotIdAndVoteType(
            Long voterId, Long mascotId, Vote.VoteType voteType);

    /**
     * 중복 투표 체크 (투표자 + 엔트리 + 투표 타입) - 새로운 방식
     */
    boolean existsByVoterIdAndEntryIdAndVoteType(
            Long voterId, Long entryId, Vote.VoteType voteType);

    /**
     * 멱등키 중복 체크
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * 멱등키로 투표 조회
     */
    Optional<Vote> findByIdempotencyKey(String idempotencyKey);

    /**
     * 마스코트별 투표 목록 조회
     */
    List<Vote> findByMascotIdOrderByCreatedAtDesc(Long mascotId);

    /**
     * 투표자별 투표 목록 조회
     */
    List<Vote> findByVoterIdOrderByCreatedAtDesc(Long voterId);

    /**
     * 투표자별 투표 타입별 투표 목록 조회
     */
    List<Vote> findByVoterIdAndVoteTypeOrderByCreatedAtDesc(Long voterId, Vote.VoteType voteType);

    /**
     * 마스코트별 총 투표 수 조회
     */
    @Query("SELECT SUM(v.weight) FROM Vote v WHERE v.mascotId = :mascotId")
    Long sumVotesByMascotId(@Param("mascotId") Long mascotId);

    /**
     * 엔트리별 총 투표 수 조회 - 새로운 방식
     */
    @Query("SELECT SUM(v.weight) FROM Vote v WHERE v.entryId = :entryId")
    Long sumVotesByEntryId(@Param("entryId") Long entryId);

    /**
     * 투표 타입별 총 투표 수 조회
     */
    @Query("SELECT SUM(v.weight) FROM Vote v WHERE v.voteType = :voteType")
    Long sumVotesByVoteType(@Param("voteType") Vote.VoteType voteType);

    /**
     * 투표자의 일일 투표 수 조회
     */
    @Query("SELECT COUNT(v) FROM Vote v " +
           "WHERE v.voterId = :voterId " +
           "AND v.voteType = :voteType " +
           "AND CAST(v.createdAt AS DATE) = CAST(:date AS DATE)")
    Long countDailyVotesByVoter(
            @Param("voterId") Long voterId,
            @Param("voteType") Vote.VoteType voteType,
            @Param("date") LocalDateTime date);

    /**
     * 마스코트별 투표 수 조회 (투표 타입별)
     */
    long countByMascotIdAndVoteType(Long mascotId, Vote.VoteType voteType);

    /**
     * 엔트리별 투표 수 조회 (투표 타입별) - 새로운 방식
     */
    long countByEntryIdAndVoteType(Long entryId, Vote.VoteType voteType);

    /**
     * 캠퍼스별 투표 수 조회
     */
    long countByCampusIdAndVoteType(Long campusId, Vote.VoteType voteType);

    /**
     * 기간별 투표 조회
     */
    Page<Vote> findByVoteTypeAndCreatedAtBetween(
            Vote.VoteType voteType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * 마스코트별 기간 투표 수 조회
     */
    @Query("SELECT SUM(v.weight) FROM Vote v " +
           "WHERE v.mascotId = :mascotId " +
           "AND v.createdAt BETWEEN :startDate AND :endDate")
    Long sumVotesByMascotIdAndPeriod(
            @Param("mascotId") Long mascotId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 투표자의 최근 투표 조회
     */
    Optional<Vote> findTopByVoterIdAndVoteTypeOrderByCreatedAtDesc(
            Long voterId, Vote.VoteType voteType);

    /**
     * 마스코트별 최근 투표 조회
     */
    Optional<Vote> findTopByMascotIdOrderByCreatedAtDesc(Long mascotId);

    /**
     * 활발한 투표자 조회 (기간별)
     */
    @Query("SELECT v.voterId, COUNT(v) as voteCount FROM Vote v " +
           "WHERE v.voteType = :voteType " +
           "AND v.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY v.voterId " +
           "ORDER BY voteCount DESC")
    Page<Object[]> findActiveVoters(
            @Param("voteType") Vote.VoteType voteType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 인기 마스코트 조회 (기간별 투표수 기준)
     */
    @Query("SELECT v.mascotId, SUM(v.weight) as totalVotes FROM Vote v " +
           "WHERE v.voteType = :voteType " +
           "AND v.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY v.mascotId " +
           "ORDER BY totalVotes DESC")
    Page<Object[]> findPopularMascotsByPeriod(
            @Param("voteType") Vote.VoteType voteType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 투표 트렌드 분석 (시간대별)
     */
    @Query("SELECT HOUR(v.createdAt) as hour, COUNT(v) as voteCount FROM Vote v " +
           "WHERE v.voteType = :voteType " +
           "AND CAST(v.createdAt AS DATE) = CAST(:date AS DATE) " +
           "GROUP BY HOUR(v.createdAt) " +
           "ORDER BY hour")
    List<Object[]> findVoteTrendsByHour(
            @Param("voteType") Vote.VoteType voteType,
            @Param("date") LocalDateTime date);
}
