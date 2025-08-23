package com.solsolhey.share.entity;

import com.solsolhey.user.entity.BaseEntity;
import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 공유 링크 엔티티
 */
@Entity
@Table(name = "share_links")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "link_code", unique = true, nullable = false, length = 20)
    private String linkCode; // 고유한 공유 코드

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "target_url", length = 1000)
    private String targetUrl; // 클릭 시 이동할 URL

    @Enumerated(EnumType.STRING)
    @Column(name = "share_type", nullable = false)
    private ShareType shareType;

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public ShareLink(User user, String linkCode, String title, String description, 
                    String thumbnailUrl, String targetUrl, ShareType shareType, LocalDateTime expiresAt) {
        this.user = user;
        this.linkCode = linkCode;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.targetUrl = targetUrl;
        this.shareType = shareType;
        this.clickCount = 0;
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    /**
     * 클릭 횟수 증가
     */
    public void incrementClickCount() {
        this.clickCount++;
    }

    /**
     * 링크 만료 확인
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 링크 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    public enum ShareType {
        CHALLENGE_INVITE,   // 챌린지 초대
        FRIEND_INVITE,      // 친구 초대
        ACHIEVEMENT,        // 업적 공유
        GENERAL            // 일반 공유
    }
}
