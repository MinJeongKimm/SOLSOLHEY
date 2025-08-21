package com.solsolhey.solsol.dto.ranking;

import lombok.Builder;
import lombok.Getter;

/**
 * 투표 응답 DTO
 */
@Getter
@Builder
public class VoteResponseDto {

    private Long entryId;
    private Long voterId;
    private String contestType;
    private String message;
    private boolean success;

    public static VoteResponseDto success(Long entryId, Long voterId, String contestType) {
        return VoteResponseDto.builder()
                .entryId(entryId)
                .voterId(voterId)
                .contestType(contestType)
                .message("투표가 성공적으로 완료되었습니다.")
                .success(true)
                .build();
    }

    public static VoteResponseDto failure(String message) {
        return VoteResponseDto.builder()
                .message(message)
                .success(false)
                .build();
    }
}
