package com.solsolhey.mascot.dto;

import java.util.List;

import com.solsolhey.mascot.domain.Background;

import lombok.Builder;
import lombok.Getter;

/**
 * 배경 목록 조회 응답 DTO
 */
@Getter
@Builder
public class BackgroundResponse {
    
    private final String id;
    private final String name;
    private final String previewUrl;
    private final Boolean enabled;
    private final List<String> tags;
    private final Boolean owned;
    
    /**
     * Background 엔티티로부터 응답 생성
     */
    public static BackgroundResponse from(Background background, boolean owned) {
        return BackgroundResponse.builder()
                .id(background.getId())
                .name(background.getName())
                .previewUrl(background.getPreviewUrl())
                .enabled(background.getEnabled())
                .tags(background.getTags())
                .owned(owned)
                .build();
    }
    
    /**
     * Background 엔티티 목록으로부터 응답 목록 생성
     */
    public static List<BackgroundResponse> fromList(List<Background> backgrounds, List<String> ownedBackgroundIds) {
        return backgrounds.stream()
                .map(background -> from(background, ownedBackgroundIds.contains(background.getId())))
                .toList();
    }
}
