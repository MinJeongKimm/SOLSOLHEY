package com.solsolhey.mascot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마스코트 배경 적용 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ApplyBackgroundRequest {
    
    @NotBlank(message = "배경 ID는 필수입니다.")
    private String backgroundId;
    
    public ApplyBackgroundRequest(String backgroundId) {
        this.backgroundId = backgroundId;
    }
}
