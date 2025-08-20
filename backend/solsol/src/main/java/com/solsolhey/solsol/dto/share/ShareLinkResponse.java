package com.solsolhey.solsol.dto.share;

import com.solsolhey.solsol.entity.ShareLink;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공유 링크 응답 DTO
 */
@Getter
@Builder
public class ShareLinkResponse {

    private final Long shareLinkId;
    private final String shareCode;
    private final String shareUrl; // 완전한 공유 URL
    private final String targetType;
    private final Long referenceId;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final Integer clickCount;
    private final String status;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    
    public static ShareLinkResponse from(ShareLink shareLink, String baseUrl) {
        String shareUrl = baseUrl + "/share/" + shareLink.getShareCode();
        
        return ShareLinkResponse.builder()
                .shareLinkId(shareLink.getShareLinkId())
                .shareCode(shareLink.getShareCode())
                .shareUrl(shareUrl)
                .targetType(shareLink.getTargetType().name())
                .referenceId(shareLink.getReferenceId())
                .title(shareLink.getTitle())
                .description(shareLink.getDescription())
                .imageUrl(shareLink.getImageUrl())
                .clickCount(shareLink.getClickCount())
                .status(shareLink.getStatus().name())
                .expiresAt(shareLink.getExpiresAt())
                .createdAt(shareLink.getCreatedAt())
                .build();
    }
}
