package com.solsolhey.share.dto.response;

import com.solsolhey.share.entity.ShareImage;
import com.solsolhey.share.entity.ShareImage.ImageType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 공유 이미지 응답 DTO
 */
@Builder
public record ShareImageResponse(
    Long imageId,
    Long userId,
    String userNickname,
    Long templateId,
    String templateName,
    String imageUrl,
    String originalFilename,
    Long fileSize,
    Integer width,
    Integer height,
    ImageType imageType,
    Integer shareCount,
    Boolean isPublic,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ShareImageResponse from(ShareImage shareImage) {
        return ShareImageResponse.builder()
                .imageId(shareImage.getImageId())
                .userId(shareImage.getUser().getUserId())
                .userNickname(shareImage.getUser().getNickname())
                .templateId(shareImage.getTemplate() != null ? shareImage.getTemplate().getTemplateId() : null)
                .templateName(shareImage.getTemplate() != null ? shareImage.getTemplate().getTemplateName() : null)
                .imageUrl(shareImage.getImageUrl())
                .originalFilename(shareImage.getOriginalFilename())
                .fileSize(shareImage.getFileSize())
                .width(shareImage.getWidth())
                .height(shareImage.getHeight())
                .imageType(shareImage.getImageType())
                .shareCount(shareImage.getShareCount())
                .isPublic(shareImage.getIsPublic())
                .createdAt(shareImage.getCreatedAt())
                .updatedAt(shareImage.getUpdatedAt())
                .build();
    }
}
