package com.solsolhey.share.dto.response;

import com.solsolhey.share.entity.ShareLink;
import com.solsolhey.share.entity.ShareLink.ShareType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 공유 링크 응답 DTO
 */
@Builder
public record ShareLinkResponse(
    Long linkId,
    Long userId,
    String userNickname,
    String linkCode,
    String title,
    String description,
    String thumbnailUrl,
    String targetUrl,
    ShareType shareType,
    Integer clickCount,
    LocalDateTime expiresAt,
    Boolean isActive,
    Boolean isExpired,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ShareLinkResponse from(ShareLink shareLink) {
        return ShareLinkResponse.builder()
                .linkId(shareLink.getLinkId())
                .userId(shareLink.getUser().getUserId())
                .userNickname(shareLink.getUser().getNickname())
                .linkCode(shareLink.getLinkCode())
                .title(shareLink.getTitle())
                .description(shareLink.getDescription())
                .thumbnailUrl(shareLink.getThumbnailUrl())
                .targetUrl(shareLink.getTargetUrl())
                .shareType(shareLink.getShareType())
                .clickCount(shareLink.getClickCount())
                .expiresAt(shareLink.getExpiresAt())
                .isActive(shareLink.getIsActive())
                .isExpired(shareLink.isExpired())
                .createdAt(shareLink.getCreatedAt())
                .updatedAt(shareLink.getUpdatedAt())
                .build();
    }
}
