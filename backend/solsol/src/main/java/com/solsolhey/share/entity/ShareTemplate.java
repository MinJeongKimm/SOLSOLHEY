package com.solsolhey.share.entity;

import com.solsolhey.user.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 공유 템플릿 엔티티
 */
@Entity
@Table(name = "share_templates")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "template_content", nullable = false, columnDefinition = "TEXT")
    private String templateContent; // HTML/JSON 템플릿

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false)
    private TemplateType templateType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "preview_url", length = 500)
    private String previewUrl;

    @Builder
    public ShareTemplate(String templateName, String templateContent, 
                        TemplateType templateType, String previewUrl) {
        this.templateName = templateName;
        this.templateContent = templateContent;
        this.templateType = templateType;
        this.previewUrl = previewUrl;
        this.isActive = true;
    }

    /**
     * 템플릿 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 템플릿 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    public enum TemplateType {
        CHALLENGE_COMPLETE,  // 챌린지 완료
        LEVEL_UP,           // 레벨업
        ACHIEVEMENT,        // 업적 달성
        FRIEND_INVITE,      // 친구 초대
        DAILY_CHECK        // 출석 체크
    }
}
