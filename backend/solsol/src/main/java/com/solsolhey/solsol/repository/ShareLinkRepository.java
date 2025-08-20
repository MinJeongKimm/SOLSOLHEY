package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.ShareLink;
import com.solsolhey.solsol.entity.User;
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
 * 공유 링크 Repository
 */
@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    /**
     * 공유 코드로 공유 링크 조회
     */
    Optional<ShareLink> findByShareCode(String shareCode);

    /**
     * 사용자의 공유 링크 목록 조회 (최신순)
     */
    List<ShareLink> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 사용자의 공유 링크 목록 조회 (페이징)
     */
    Page<ShareLink> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 사용자의 활성 공유 링크 목록 조회
     */
    List<ShareLink> findByUserAndStatusOrderByCreatedAtDesc(User user, ShareLink.ShareStatus status);

    /**
     * 특정 타입의 공유 링크 조회
     */
    List<ShareLink> findByTargetTypeAndStatusOrderByCreatedAtDesc(
            ShareLink.ShareTargetType targetType, ShareLink.ShareStatus status);

    /**
     * 특정 사용자의 특정 타입 공유 링크 조회
     */
    List<ShareLink> findByUserAndTargetTypeAndStatusOrderByCreatedAtDesc(
            User user, ShareLink.ShareTargetType targetType, ShareLink.ShareStatus status);

    /**
     * 만료된 공유 링크들 조회
     */
    @Query("SELECT sl FROM ShareLink sl WHERE sl.expiresAt < :now AND sl.status = 'ACTIVE'")
    List<ShareLink> findExpiredLinks(@Param("now") LocalDateTime now);

    /**
     * 공유 코드 존재 여부 확인
     */
    boolean existsByShareCode(String shareCode);

    /**
     * 사용자별 공유 통계 - 총 생성한 공유 수
     */
    long countByUser(User user);

    /**
     * 사용자별 공유 통계 - 활성 공유 수
     */
    long countByUserAndStatus(User user, ShareLink.ShareStatus status);

    /**
     * 사용자별 공유 통계 - 총 클릭 수
     */
    @Query("SELECT COALESCE(SUM(sl.clickCount), 0) FROM ShareLink sl WHERE sl.user = :user")
    Long getTotalClicksByUser(@Param("user") User user);

    /**
     * 타입별 공유 통계
     */
    @Query("SELECT sl.targetType, COUNT(sl), COALESCE(SUM(sl.clickCount), 0) " +
           "FROM ShareLink sl WHERE sl.user = :user " +
           "GROUP BY sl.targetType")
    List<Object[]> getShareStatsByType(@Param("user") User user);

    /**
     * 인기 공유 링크 조회 (클릭 수 기준 상위)
     */
    List<ShareLink> findTop10ByStatusOrderByClickCountDesc(ShareLink.ShareStatus status);

    /**
     * 특정 참조 ID와 타입으로 공유 링크 조회
     */
    List<ShareLink> findByUserAndTargetTypeAndReferenceIdAndStatus(
            User user, ShareLink.ShareTargetType targetType, Long referenceId, ShareLink.ShareStatus status);

    /**
     * 특정 기간 내 생성된 공유 링크들 조회
     */
    @Query("SELECT sl FROM ShareLink sl WHERE sl.user = :user " +
           "AND sl.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sl.createdAt DESC")
    List<ShareLink> findByUserAndCreatedAtBetween(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
