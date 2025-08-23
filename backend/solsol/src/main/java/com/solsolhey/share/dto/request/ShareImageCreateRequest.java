package com.solsolhey.share.dto.request;

import com.solsolhey.share.entity.ShareImage.ImageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * 공유 이미지 생성 요청 DTO
 */
public record ShareImageCreateRequest(
    Long templateId,
    
    @NotBlank(message = "이미지 URL은 필수입니다.")
    @Pattern(regexp = "^https?://.*", message = "올바른 URL 형식이어야 합니다.")
    @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
    String imageUrl,
    
    @Size(max = 255, message = "파일명은 255자 이하여야 합니다.")
    String originalFilename,
    
    Long fileSize,
    
    Integer width,
    
    Integer height,
    
    @NotNull(message = "이미지 타입은 필수입니다.")
    ImageType imageType,
    
    Boolean isPublic
) {
    public ShareImageCreateRequest {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }
        if (imageType == null) {
            throw new IllegalArgumentException("이미지 타입은 필수입니다.");
        }
        // isPublic이 null이면 기본값 true로 설정
        if (isPublic == null) {
            isPublic = true;
        }
    }
}
