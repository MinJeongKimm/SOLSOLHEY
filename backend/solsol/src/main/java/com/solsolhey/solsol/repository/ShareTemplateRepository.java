package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.ShareTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 공유 템플릿 Repository
 */
@Repository
public interface ShareTemplateRepository extends JpaRepository<ShareTemplate, Long> {

    /**
     * 템플릿 이름으로 조회
     */
    Optional<ShareTemplate> findByTemplateName(String templateName);

    /**
     * 활성화된 템플릿 목록 조회
     */
    List<ShareTemplate> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 특정 타입의 활성화된 템플릿들 조회
     */
    List<ShareTemplate> findByTemplateTypeAndIsActiveTrueOrderByCreatedAtDesc(
            ShareTemplate.TemplateType templateType);

    /**
     * 템플릿 이름 존재 여부 확인
     */
    boolean existsByTemplateName(String templateName);

    /**
     * 모든 템플릿 조회 (관리자용)
     */
    List<ShareTemplate> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 타입의 모든 템플릿들 조회
     */
    List<ShareTemplate> findByTemplateTypeOrderByCreatedAtDesc(ShareTemplate.TemplateType templateType);
}
