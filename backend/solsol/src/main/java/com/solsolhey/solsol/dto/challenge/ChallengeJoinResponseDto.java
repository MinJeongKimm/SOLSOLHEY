package com.solsolhey.solsol.dto.challenge;

import com.solsolhey.solsol.entity.UserChallenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 참여 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeJoinResponseDto {

    private Boolean success;
    private String message;
    private Long userChallengeId;
    private String status;
    private UserChallengeDto userChallenge;

    public static ChallengeJoinResponseDto success(UserChallenge userChallenge) {
        return ChallengeJoinResponseDto.builder()
                .success(true)
                .message("챌린지 참여가 완료되었습니다.")
                .userChallengeId(userChallenge.getUserChallengeId())
                .status(userChallenge.getStatus().name())
                .userChallenge(UserChallengeDto.from(userChallenge))
                .build();
    }

    public static ChallengeJoinResponseDto failure(String message) {
        return ChallengeJoinResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
}
