package com.solsolhey.share.repository;

import com.solsolhey.share.entity.ShareLink;
import com.solsolhey.share.entity.ShareLink.ShareType;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * 링크 코드로 조회
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<ShareLink> findByLinkCodeAndIsActiveTrue(String linkCode);

    /**
     * 사용자별 공유 링크 조회
     */
    @Query("SELECT sl FROM ShareLink sl WHERE sl.user = :user ORDER BY sl.createdAt DESC")
    Page<ShareLink> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    /**
     * 공유 타입별 조회
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT sl FROM ShareLink sl WHERE sl.shareType = :type AND sl.isActive = true")
    List<ShareLink> findByShareTypeAndIsActiveTrue(@Param("type") ShareType type);

    /**
     * 만료된 링크 조회
     */
    @Query("SELECT sl FROM ShareLink sl WHERE sl.expiresAt <= :now AND sl.isActive = true")
    List<ShareLink> findExpiredLinks(@Param("now") LocalDateTime now);

    /**
     * 인기 링크 조회 (클릭수 기준)
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT sl FROM ShareLink sl WHERE sl.isActive = true ORDER BY sl.clickCount DESC")
    List<ShareLink> findTopClickedLinks(Pageable pageable);

    /**
     * 특정 기간 내 생성된 링크 조회
     */
    @Query("SELECT sl FROM ShareLink sl WHERE sl.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sl.createdAt DESC")
    List<ShareLink> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 총 클릭수 조회
     */
    @Query("SELECT COALESCE(SUM(sl.clickCount), 0) FROM ShareLink sl WHERE sl.user = :user")
    Long getTotalClicksByUser(@Param("user") User user);

    /**
     * 링크 코드 중복 확인
     */
    boolean existsByLinkCode(String linkCode);
}
