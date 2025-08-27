package com.solsolhey.ranking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.solsolhey.ranking.entity.RankingEntry;

/**
 * 랭킹 참가 정보를 위한 Repository
 */
@Repository
public interface RankingEntryRepository extends JpaRepository<RankingEntry, Long> {

    /**
     * 사용자별 참가 목록 조회 (최신순)
     */
    @Query("SELECT re FROM RankingEntry re WHERE re.userId = :userId ORDER BY re.createdAt DESC")
    List<RankingEntry> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 사용자의 참가 개수 조회
     */
    @Query("SELECT COUNT(re) FROM RankingEntry re WHERE re.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 특정 마스코트 스냅샷이 이미 참가되어 있는지 확인
     */
    @Query("SELECT COUNT(re) > 0 FROM RankingEntry re WHERE re.mascotSnapshotId = :mascotSnapshotId")
    boolean existsByMascotSnapshotId(@Param("mascotSnapshotId") Long mascotSnapshotId);

    /**
     * 사용자가 특정 마스코트 스냅샷을 이미 참가했는지 확인
     */
    @Query("SELECT COUNT(re) > 0 FROM RankingEntry re WHERE re.userId = :userId AND re.mascotSnapshotId = :mascotSnapshotId")
    boolean existsByUserIdAndMascotSnapshotId(@Param("userId") Long userId, @Param("mascotSnapshotId") Long mascotSnapshotId);

    /**
     * 전체 랭킹 보드 조회 (점수 내림차순, 최신순)
     */
    @Query("SELECT re FROM RankingEntry re ORDER BY re.score DESC, re.createdAt ASC")
    Page<RankingEntry> findAllOrderByScoreDescAndCreatedAtAsc(Pageable pageable);

    /**
     * 특정 점수 이상의 참가 목록 조회
     */
    @Query("SELECT re FROM RankingEntry re WHERE re.score >= :minScore ORDER BY re.score DESC, re.createdAt ASC")
    Page<RankingEntry> findByScoreGreaterThanEqualOrderByScoreDesc(@Param("minScore") Integer minScore, Pageable pageable);

    /**
     * 사용자별 최고 점수 참가 조회
     */
    @Query("SELECT re FROM RankingEntry re WHERE re.userId = :userId ORDER BY re.score DESC LIMIT 1")
    Optional<RankingEntry> findTopByUserIdOrderByScoreDesc(@Param("userId") Long userId);
}
