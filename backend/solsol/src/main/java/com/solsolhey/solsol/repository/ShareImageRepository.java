package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.ShareImage;
import com.solsolhey.solsol.entity.ShareTemplate;
import com.solsolhey.solsol.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공유 이미지 Repository
 */
@Repository
public interface ShareImageRepository extends JpaRepository<ShareImage, Long> {

    /**
     * 사용자의 공유 이미지 목록 조회 (최신순)
     */
    List<ShareImage> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 사용자의 공유 이미지 목록 조회 (페이징)
     */
    Page<ShareImage> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 사용자의 활성 공유 이미지 목록 조회
     */
    List<ShareImage> findByUserAndStatusOrderByCreatedAtDesc(User user, ShareImage.ImageStatus status);

    /**
     * 특정 템플릿으로 생성된 이미지들 조회
     */
    List<ShareImage> findByTemplateAndStatusOrderByCreatedAtDesc(
            ShareTemplate template, ShareImage.ImageStatus status);

    /**
     * 사용자별 이미지 통계 - 총 생성한 이미지 수
     */
    long countByUser(User user);

    /**
     * 사용자별 이미지 통계 - 활성 이미지 수
     */
    long countByUserAndStatus(User user, ShareImage.ImageStatus status);

    /**
     * 사용자별 다운로드 통계 - 총 다운로드 수
     */
    @Query("SELECT COALESCE(SUM(si.downloadCount), 0) FROM ShareImage si WHERE si.user = :user")
    Long getTotalDownloadsByUser(@Param("user") User user);

    /**
     * 템플릿별 이미지 통계
     */
    @Query("SELECT si.template.templateName, COUNT(si), COALESCE(SUM(si.downloadCount), 0) " +
           "FROM ShareImage si WHERE si.user = :user " +
           "GROUP BY si.template.templateName")
    List<Object[]> getImageStatsByTemplate(@Param("user") User user);

    /**
     * 인기 이미지 조회 (다운로드 수 기준 상위)
     */
    List<ShareImage> findTop10ByStatusOrderByDownloadCountDesc(ShareImage.ImageStatus status);

    /**
     * 특정 기간 내 생성된 이미지들 조회
     */
    @Query("SELECT si FROM ShareImage si WHERE si.user = :user " +
           "AND si.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY si.createdAt DESC")
    List<ShareImage> findByUserAndCreatedAtBetween(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 전체 이미지 파일 크기 통계
     */
    @Query("SELECT COALESCE(SUM(si.fileSize), 0) FROM ShareImage si WHERE si.user = :user AND si.status = 'ACTIVE'")
    Long getTotalFileSizeByUser(@Param("user") User user);

    /**
     * 템플릿별 사용 횟수
     */
    @Query("SELECT si.template, COUNT(si) FROM ShareImage si " +
           "WHERE si.status = 'ACTIVE' " +
           "GROUP BY si.template " +
           "ORDER BY COUNT(si) DESC")
    List<Object[]> getTemplateUsageStats();
}
