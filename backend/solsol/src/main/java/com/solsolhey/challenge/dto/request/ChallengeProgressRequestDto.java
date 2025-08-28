package com.solsolhey.challenge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 진행도 갱신 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeProgressRequestDto {

    @NotNull(message = "진행 단계는 필수입니다.")
    private Integer step;

    private String payload; // 추가 데이터 (JSON 문자열)

    // 편의 메서드
    public int getStepValue() {
        return step != null ? step : 0;
    }
}
