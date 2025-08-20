package com.solsolhey.solsol.dto.share;

import com.solsolhey.solsol.entity.ShareLink;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공유 링크 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ShareLinkCreateRequest {

    @NotNull(message = "공유 대상 타입은 필수입니다.")
    private ShareLink.ShareTargetType target;

    private Long refId; // 참조 ID (선택사항)

    private String title; // 공유 제목 (선택사항)
    
    private String description; // 공유 설명 (선택사항)
    
    private String imageUrl; // 공유 이미지 URL (선택사항)
    
    private Integer expiresInHours; // 만료 시간 (시간 단위, 선택사항)

    public ShareLinkCreateRequest(ShareLink.ShareTargetType target, Long refId) {
        this.target = target;
        this.refId = refId;
    }

    public ShareLinkCreateRequest(ShareLink.ShareTargetType target, Long refId, String title, 
                                 String description, String imageUrl, Integer expiresInHours) {
        this.target = target;
        this.refId = refId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.expiresInHours = expiresInHours;
    }
}
