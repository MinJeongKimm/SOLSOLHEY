package com.solsolhey.mascot.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.solsolhey.mascot.domain.Background;
import com.solsolhey.mascot.domain.UserBackground;

import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 보유 배경 조회 응답 DTO
 */
@Getter
@Builder
public class UserBackgroundResponse {
    
    private final String id;
    private final String name;
    private final String previewUrl;
    private final String acquiredAt;
    
    /**
     * UserBackground와 Background 엔티티로부터 응답 생성
     */
    public static UserBackgroundResponse from(UserBackground userBackground, Background background) {
        return UserBackgroundResponse.builder()
                .id(background.getId())
                .name(background.getName())
                .previewUrl(background.getPreviewUrl())
                .acquiredAt(userBackground.getAcquiredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
    
    /**
     * UserBackground 목록과 Background 목록으로부터 응답 목록 생성
     */
    public static List<UserBackgroundResponse> fromLists(List<UserBackground> userBackgrounds, 
                                                        List<Background> backgrounds) {
        return userBackgrounds.stream()
                .map(userBackground -> {
                    Background background = backgrounds.stream()
                            .filter(bg -> bg.getId().equals(userBackground.getBackgroundId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (background != null) {
                        return from(userBackground, background);
                    }
                    return null;
                })
                .filter(response -> response != null)
                .toList();
    }
}
