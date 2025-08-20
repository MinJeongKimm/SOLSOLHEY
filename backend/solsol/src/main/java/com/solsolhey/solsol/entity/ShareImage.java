package com.solsolhey.solsol.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 생성된 공유 이미지를 기록하는 엔티티
 */
@Entity
@Table(name = "share_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_image_id")
    private Long shareImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 이미지를 생성한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ShareTemplate template; // 사용된 템플릿

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl; // 생성된 이미지 URL

    @Column(name = "image_data", columnDefinition = "TEXT")
    private String imageData; // 이미지 생성에 사용된 데이터 (JSON 형태)

    @Column(name = "file_size")
    private Long fileSize; // 파일 크기 (bytes)

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0; // 다운로드 횟수

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ImageStatus status = ImageStatus.ACTIVE;

    @Builder
    public ShareImage(User user, ShareTemplate template, String imageUrl, 
                     String imageData, Long fileSize) {
        this.user = user;
        this.template = template;
        this.imageUrl = imageUrl;
        this.imageData = imageData;
        this.fileSize = fileSize;
        this.downloadCount = 0;
        this.status = ImageStatus.ACTIVE;
    }

    /**
     * 이미지 상태
     */
    public enum ImageStatus {
        ACTIVE,   // 활성 상태
        DELETED   // 삭제됨
    }

    /**
     * 다운로드 횟수 증가
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * 이미지 삭제 표시
     */
    public void markAsDeleted() {
        this.status = ImageStatus.DELETED;
    }

    /**
     * 이미지 활성화
     */
    public void activate() {
        this.status = ImageStatus.ACTIVE;
    }

    /**
     * 이미지가 활성 상태인지 확인
     */
    public boolean isActive() {
        return this.status == ImageStatus.ACTIVE;
    }
}
