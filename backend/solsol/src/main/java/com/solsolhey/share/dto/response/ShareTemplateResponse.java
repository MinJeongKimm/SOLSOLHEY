package com.solsolhey.share.dto.response;

import com.solsolhey.share.entity.ShareTemplate;
import com.solsolhey.share.entity.ShareTemplate.TemplateType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 공유 템플릿 응답 DTO
 */
@Builder
public record ShareTemplateResponse(
    Long templateId,
    String templateName,
    String templateContent,
    TemplateType templateType,
    Boolean isActive,
    String previewUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ShareTemplateResponse from(ShareTemplate template) {
        return ShareTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .templateName(template.getTemplateName())
                .templateContent(template.getTemplateContent())
                .templateType(template.getTemplateType())
                .isActive(template.getIsActive())
                .previewUrl(template.getPreviewUrl())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
