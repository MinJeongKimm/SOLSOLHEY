package com.solsolhey.ranking.repository;

import java.util.List;

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
     * 전체 랭킹 보드 조회 (생성일 내림차순)
     */
    @Query("SELECT re FROM RankingEntry re ORDER BY re.createdAt DESC")
    Page<RankingEntry> findAllOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 사용자의 특정 타입 참가 목록 조회 (최신순)
     */
    @Query("SELECT re FROM RankingEntry re WHERE re.userId = :userId AND re.rankingType = :rankingType ORDER BY re.createdAt DESC")
    List<RankingEntry> findByUserIdAndRankingTypeOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("rankingType") String rankingType);

    /**
     * 특정 마스코트 ID로 RankingEntry가 존재하는지 확인
     */
    @Query("SELECT COUNT(re) > 0 FROM RankingEntry re WHERE re.mascotId = :mascotId")
    boolean existsByMascotId(@Param("mascotId") Long mascotId);

    /**
     * 특정 마스코트 ID와 랭킹 타입으로 RankingEntry가 존재하는지 확인
     */
    @Query("SELECT COUNT(re) > 0 FROM RankingEntry re WHERE re.mascotId = :mascotId AND re.rankingType = :rankingType")
    boolean existsByMascotIdAndRankingType(@Param("mascotId") Long mascotId, @Param("rankingType") String rankingType);

    /**
     * 마스코트 ID 목록과 랭킹 타입으로 RankingEntry 조회
     */
    @Query("SELECT re FROM RankingEntry re WHERE re.mascotId IN :mascotIds AND re.rankingType = :rankingType ORDER BY re.createdAt DESC")
    List<RankingEntry> findByMascotIdInAndRankingType(@Param("mascotIds") List<Long> mascotIds, @Param("rankingType") String rankingType);
}
