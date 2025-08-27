package com.solsolhey.ranking.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.Getter;

/**
 * 투표 응답 DTO (마스코트 기반)
 */
@Getter
@Builder
public class VoteResponse {

    private final Boolean success;        // 요청 성공 여부
    private final Long mascotId;          // 투표 대상 마스코트 ID
    private final Long campusId;          // 캠퍼스 ID (교내 투표용)
    private final Long newVotes;          // 반영 후 총 득표 수
    private final String votedAt;         // 반영 시각 (ISO 8601)
    private final String message;         // 결과 메시지
    private final String idempotencyKey;  // 멱등키 (중복/충돌 시 포함)

    /**
     * 성공 응답 생성 (교내 투표)
     */
    public static VoteResponse successForCampus(Long mascotId, Long campusId, 
                                               Long newVotes, LocalDateTime votedAt) {
        return VoteResponse.builder()
                .success(true)
                .mascotId(mascotId)
                .campusId(campusId)
                .newVotes(newVotes)
                .votedAt(votedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .message("투표가 성공적으로 처리되었습니다.")
                .build();
    }

    /**
     * 성공 응답 생성 (전국 투표)
     */
    public static VoteResponse successForNational(Long mascotId, Long newVotes, 
                                                LocalDateTime votedAt) {
        return VoteResponse.builder()
                .success(true)
                .mascotId(mascotId)
                .newVotes(newVotes)
                .votedAt(votedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .message("투표가 성공적으로 처리되었습니다.")
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static VoteResponse failure(String message) {
        return VoteResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 실패 응답 생성 (멱등키 포함)
     */
    public static VoteResponse failure(String message, String idempotencyKey) {
        return VoteResponse.builder()
                .success(false)
                .message(message)
                .idempotencyKey(idempotencyKey)
                .build();
    }
}
