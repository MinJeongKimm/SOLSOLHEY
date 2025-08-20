package com.solsolhey.solsol.dto.share;

import com.solsolhey.solsol.entity.ShareImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공유 이미지 응답 DTO
 */
@Getter
@Builder
public class ShareImageResponse {

    private final Long shareImageId;
    private final String imageUrl;
    private final String templateName;
    private final Long fileSize;
    private final Integer downloadCount;
    private final String status;
    private final LocalDateTime createdAt;
    
    public static ShareImageResponse from(ShareImage shareImage) {
        return ShareImageResponse.builder()
                .shareImageId(shareImage.getShareImageId())
                .imageUrl(shareImage.getImageUrl())
                .templateName(shareImage.getTemplate().getTemplateName())
                .fileSize(shareImage.getFileSize())
                .downloadCount(shareImage.getDownloadCount())
                .status(shareImage.getStatus().name())
                .createdAt(shareImage.getCreatedAt())
                .build();
    }
}
