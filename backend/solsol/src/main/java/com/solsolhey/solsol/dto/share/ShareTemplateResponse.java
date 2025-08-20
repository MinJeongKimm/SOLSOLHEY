package com.solsolhey.solsol.dto.share;

import com.solsolhey.solsol.entity.ShareTemplate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공유 템플릿 응답 DTO
 */
@Getter
@Builder
public class ShareTemplateResponse {

    private Long templateId;
    private String templateName;
    private String templateType;
    private String templateUrl;
    private Integer width;
    private Integer height;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    public static ShareTemplateResponse from(ShareTemplate template) {
        return ShareTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .templateName(template.getTemplateName())
                .templateType(template.getTemplateType().name())
                .templateUrl(template.getTemplateUrl())
                .width(template.getWidth())
                .height(template.getHeight())
                .description(template.getDescription())
                .isActive(template.getIsActive())
                .createdAt(template.getCreatedAt())
                .build();
    }
    
    public static List<ShareTemplateResponse> fromList(List<ShareTemplate> templates) {
        return templates.stream()
                .map(ShareTemplateResponse::from)
                .toList();
    }
}
