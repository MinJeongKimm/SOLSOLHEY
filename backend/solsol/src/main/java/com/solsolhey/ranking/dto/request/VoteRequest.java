package com.solsolhey.ranking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 투표 요청 DTO (교내/전국 공통, 마스코트 기반)
 */
@Getter
@Setter
@NoArgsConstructor
public class VoteRequest {

    /**
     * 대상 캠퍼스 ID (교내 투표용, 여러 캠퍼스 운영 시 명시)
     */
    private Long campusId;

    /**
     * 투표 가중치 (기본 1, 운영/마스터만 허용)
     */
    @Min(value = 1, message = "투표 가중치는 1 이상이어야 합니다.")
    private Integer weight = 1;

    /**
     * 멱등키 (중복 요청 방지용 UUID 등)
     */
    @Size(max = 100, message = "멱등키는 100자 이하여야 합니다.")
    private String idempotencyKey;

    public VoteRequest(Long campusId, Integer weight, String idempotencyKey) {
        this.campusId = campusId;
        this.weight = weight != null ? weight : 1;
        this.idempotencyKey = idempotencyKey;
    }
}
