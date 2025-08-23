package com.solsolhey.share.repository;

import com.solsolhey.share.entity.ShareImage;
import com.solsolhey.share.entity.ShareImage.ImageType;
import com.solsolhey.share.entity.ShareTemplate;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 공유 이미지 Repository
 */
@Repository
public interface ShareImageRepository extends JpaRepository<ShareImage, Long> {

    /**
     * 사용자별 공유 이미지 조회
     */
    @EntityGraph(attributePaths = {"template"})
    @Query("SELECT si FROM ShareImage si WHERE si.user = :user ORDER BY si.createdAt DESC")
    Page<ShareImage> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    /**
     * 공개 이미지 조회
     */
    @EntityGraph(attributePaths = {"user", "template"})
    @Query("SELECT si FROM ShareImage si WHERE si.isPublic = true ORDER BY si.shareCount DESC, si.createdAt DESC")
    Page<ShareImage> findPublicImagesOrderByPopularity(Pageable pageable);

    /**
     * 이미지 타입별 조회
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT si FROM ShareImage si WHERE si.imageType = :type AND si.isPublic = true")
    List<ShareImage> findByImageTypeAndIsPublicTrue(@Param("type") ImageType type);

    /**
     * 템플릿별 이미지 조회
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT si FROM ShareImage si WHERE si.template = :template")
    List<ShareImage> findByTemplate(@Param("template") ShareTemplate template);

    /**
     * 인기 이미지 조회 (공유 수 기준)
     */
    @EntityGraph(attributePaths = {"user", "template"})
    @Query("SELECT si FROM ShareImage si WHERE si.isPublic = true ORDER BY si.shareCount DESC")
    List<ShareImage> findTopSharedImages(Pageable pageable);

    /**
     * 사용자의 총 공유 수 조회
     */
    @Query("SELECT COALESCE(SUM(si.shareCount), 0) FROM ShareImage si WHERE si.user = :user")
    Long getTotalSharesByUser(@Param("user") User user);

    /**
     * 큰 파일 크기 이미지 조회 (용량 관리용)
     */
    @Query("SELECT si FROM ShareImage si WHERE si.fileSize > :sizeThreshold ORDER BY si.fileSize DESC")
    List<ShareImage> findLargeImages(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * 사용자의 특정 타입 이미지 수 조회
     */
    @Query("SELECT COUNT(si) FROM ShareImage si WHERE si.user = :user AND si.imageType = :type")
    Long countByUserAndImageType(@Param("user") User user, @Param("type") ImageType type);
}
