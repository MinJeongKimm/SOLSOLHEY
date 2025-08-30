package com.solsolhey.mascot.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.Getter;

/**
 * 마스코트 배경 적용 응답 DTO
 */
@Getter
@Builder
public class ApplyBackgroundResponse {
    
    private final Boolean success;
    private final Long mascotId;
    private final String backgroundId;
    private final String updatedAt;
    
    /**
     * 성공 응답 생성
     */
    public static ApplyBackgroundResponse success(Long mascotId, String backgroundId, LocalDateTime updatedAt) {
        return ApplyBackgroundResponse.builder()
                .success(true)
                .mascotId(mascotId)
                .backgroundId(backgroundId)
                .updatedAt(updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static ApplyBackgroundResponse failure() {
        return ApplyBackgroundResponse.builder()
                .success(false)
                .build();
    }
}
