package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 공유 이미지 템플릿을 관리하는 엔티티
 */
@Entity
@Table(name = "share_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name", unique = true, nullable = false, length = 100)
    private String templateName; // 템플릿 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 30)
    private TemplateType templateType; // 템플릿 타입

    @Column(name = "template_url", nullable = false, length = 500)
    private String templateUrl; // 템플릿 이미지 URL

    @Column(name = "width", nullable = false)
    private Integer width; // 이미지 너비

    @Column(name = "height", nullable = false)
    private Integer height; // 이미지 높이

    @Column(name = "description", length = 300)
    private String description; // 템플릿 설명

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 활성화 여부

    @Builder
    public ShareTemplate(String templateName, TemplateType templateType, String templateUrl,
                        Integer width, Integer height, String description) {
        this.templateName = templateName;
        this.templateType = templateType;
        this.templateUrl = templateUrl;
        this.width = width;
        this.height = height;
        this.description = description;
        this.isActive = true;
    }

    /**
     * 템플릿 타입
     */
    public enum TemplateType {
        MASCOT_SHARE,     // 마스코트 공유용
        CHALLENGE_SHARE,  // 챌린지 공유용
        ACHIEVEMENT_SHARE, // 성취 공유용
        GENERAL_SHARE     // 일반 공유용
    }

    /**
     * 템플릿 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 템플릿 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 템플릿 정보 업데이트
     */
    public void updateTemplate(String templateUrl, Integer width, Integer height, String description) {
        if (templateUrl != null) this.templateUrl = templateUrl;
        if (width != null) this.width = width;
        if (height != null) this.height = height;
        if (description != null) this.description = description;
    }
}
