package com.solsolhey.share.dto.request;

import com.solsolhey.share.entity.ShareLink.ShareType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * 공유 링크 생성 요청 DTO
 */
public record ShareLinkCreateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    String title,
    
    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    String description,
    
    @Pattern(regexp = "^https?://.*", message = "올바른 URL 형식이어야 합니다.")
    @Size(max = 500, message = "썸네일 URL은 500자 이하여야 합니다.")
    String thumbnailUrl,
    
    @Pattern(regexp = "^https?://.*", message = "올바른 URL 형식이어야 합니다.")
    @Size(max = 1000, message = "대상 URL은 1000자 이하여야 합니다.")
    String targetUrl,
    
    @NotNull(message = "공유 타입은 필수입니다.")
    ShareType shareType,
    
    LocalDateTime expiresAt
) {
    public ShareLinkCreateRequest {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (shareType == null) {
            throw new IllegalArgumentException("공유 타입은 필수입니다.");
        }
    }
}
