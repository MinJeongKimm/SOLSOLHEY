package com.solsolhey.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 투표 응답 DTO
 */
@Getter
@Builder
public class VoteResponse {

    private final Boolean success;        // 요청 성공 여부
    private final Long entryId;           // 투표 대상 엔트리 ID
    private final Long campusId;          // 캠퍼스 ID (교내 투표용)
    private final Long contestId;         // 콘테스트 ID
    private final Integer newVotes;       // 반영 후 총 득표 수
    private final String votedAt;         // 반영 시각 (ISO 8601)
    private final String message;         // 결과 메시지
    private final String idempotencyKey;  // 멱등키 (중복/충돌 시 포함)

    /**
     * 성공 응답 생성 (교내 투표)
     */
    public static VoteResponse successForCampus(Long entryId, Long campusId, Long contestId, 
                                               Integer newVotes, LocalDateTime votedAt) {
        return VoteResponse.builder()
                .success(true)
                .entryId(entryId)
                .campusId(campusId)
                .contestId(contestId)
                .newVotes(newVotes)
                .votedAt(votedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .message("투표가 성공적으로 처리되었습니다.")
                .build();
    }

    /**
     * 성공 응답 생성 (전국 투표)
     */
    public static VoteResponse successForNational(Long entryId, Long contestId, 
                                                Integer newVotes, LocalDateTime votedAt) {
        return VoteResponse.builder()
                .success(true)
                .entryId(entryId)
                .contestId(contestId)
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
