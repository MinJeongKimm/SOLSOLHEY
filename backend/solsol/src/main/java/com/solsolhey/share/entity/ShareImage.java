package com.solsolhey.share.entity;

import com.solsolhey.user.entity.BaseEntity;
import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 공유 이미지 엔티티
 */
@Entity
@Table(name = "share_images")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ShareTemplate template;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType;

    @Column(name = "share_count", nullable = false)
    private Integer shareCount = 0;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Builder
    public ShareImage(User user, ShareTemplate template, String imageUrl, String originalFilename,
                     Long fileSize, Integer width, Integer height, ImageType imageType) {
        this.user = user;
        this.template = template;
        this.imageUrl = imageUrl;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.imageType = imageType;
        this.shareCount = 0;
        this.isPublic = true;
    }

    /**
     * 공유 횟수 증가
     */
    public void incrementShareCount() {
        this.shareCount++;
    }

    /**
     * 이미지를 비공개로 설정
     */
    public void makePrivate() {
        this.isPublic = false;
    }

    /**
     * 이미지를 공개로 설정
     */
    public void makePublic() {
        this.isPublic = true;
    }

    public enum ImageType {
        CHALLENGE_CARD,     // 챌린지 카드
        ACHIEVEMENT_BADGE,  // 업적 배지
        MASCOT_SHARE,      // 마스코트 공유
        CUSTOM             // 커스텀 이미지
    }
}
