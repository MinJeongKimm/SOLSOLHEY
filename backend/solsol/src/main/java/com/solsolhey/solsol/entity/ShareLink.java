package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 소셜 공유 링크를 관리하는 엔티티
 */
@Entity
@Table(name = "share_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_link_id")
    private Long shareLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 공유한 사용자

    @Column(name = "share_code", unique = true, nullable = false, length = 100)
    private String shareCode; // 공유 코드 (UUID 기반)

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ShareTargetType targetType; // 공유 대상 타입

    @Column(name = "reference_id")
    private Long referenceId; // 대상 참조 ID

    @Column(name = "title", length = 200)
    private String title; // 공유 제목

    @Column(name = "description", length = 500)
    private String description; // 공유 설명

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 공유 이미지 URL

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0; // 클릭 횟수

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // 만료 시간 (선택사항)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShareStatus status = ShareStatus.ACTIVE;

    @Builder
    public ShareLink(User user, ShareTargetType targetType, Long referenceId, 
                    String title, String description, String imageUrl, LocalDateTime expiresAt) {
        this.user = user;
        this.shareCode = UUID.randomUUID().toString();
        this.targetType = targetType;
        this.referenceId = referenceId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.expiresAt = expiresAt;
        this.clickCount = 0;
        this.status = ShareStatus.ACTIVE;
    }

    /**
     * 공유 대상 타입
     */
    public enum ShareTargetType {
        MASCOT,     // 마스코트 공유
        CHALLENGE,  // 챌린지 공유
        PROFILE,    // 프로필 공유
        ACHIEVEMENT // 성취 공유
    }

    /**
     * 공유 상태
     */
    public enum ShareStatus {
        ACTIVE,   // 활성 상태
        INACTIVE, // 비활성 상태
        EXPIRED   // 만료됨
    }

    /**
     * 클릭 증가
     */
    public void incrementClickCount() {
        this.clickCount++;
    }

    /**
     * 공유 링크 비활성화
     */
    public void deactivate() {
        this.status = ShareStatus.INACTIVE;
    }

    /**
     * 공유 링크 활성화
     */
    public void activate() {
        this.status = ShareStatus.ACTIVE;
    }

    /**
     * 만료 확인
     */
    public boolean isExpired() {
        return (this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt)) 
               || this.status == ShareStatus.EXPIRED;
    }

    /**
     * 사용 가능 확인
     */
    public boolean isUsable() {
        return this.status == ShareStatus.ACTIVE && !isExpired();
    }
}
