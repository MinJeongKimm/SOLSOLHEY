package com.solsolhey.share.repository;

import com.solsolhey.share.entity.ShareTemplate;
import com.solsolhey.share.entity.ShareTemplate.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 공유 템플릿 Repository
 */
@Repository
public interface ShareTemplateRepository extends JpaRepository<ShareTemplate, Long> {

    /**
     * 활성화된 템플릿만 조회
     */
    @Query("SELECT st FROM ShareTemplate st WHERE st.isActive = true ORDER BY st.createdAt DESC")
    List<ShareTemplate> findActiveTemplates();

    /**
     * 템플릿 타입별 조회
     */
    @Query("SELECT st FROM ShareTemplate st WHERE st.templateType = :type AND st.isActive = true")
    List<ShareTemplate> findByTemplateTypeAndActive(@Param("type") TemplateType type);

    /**
     * 템플릿 이름으로 검색
     */
    @Query("SELECT st FROM ShareTemplate st WHERE LOWER(st.templateName) LIKE LOWER(CONCAT('%', :name, '%')) AND st.isActive = true")
    List<ShareTemplate> findByTemplateNameContainingIgnoreCase(@Param("name") String name);
}
