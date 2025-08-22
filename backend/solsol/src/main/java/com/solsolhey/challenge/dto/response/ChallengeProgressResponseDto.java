package com.solsolhey.challenge.dto.response;

import com.solsolhey.challenge.entity.UserChallenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 진행도 갱신 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeProgressResponseDto {

    private Boolean success;
    private String message;
    private UserChallengeDto userChallenge;
    private Boolean isCompleted;
    private Integer rewardPoints;
    private Integer rewardExp;

    public static ChallengeProgressResponseDto success(UserChallenge userChallenge) {
        boolean isCompleted = userChallenge.isCompleted();
        
        return ChallengeProgressResponseDto.builder()
                .success(true)
                .message(isCompleted ? "챌린지를 완료했습니다! 보상이 지급되었습니다." : "진행도가 업데이트되었습니다.")
                .userChallenge(UserChallengeDto.from(userChallenge))
                .isCompleted(isCompleted)
                .rewardPoints(isCompleted ? userChallenge.getChallenge().getRewardPoints() : null)
                .rewardExp(isCompleted ? userChallenge.getChallenge().getRewardExp() : null)
                .build();
    }

    public static ChallengeProgressResponseDto failure(String message) {
        return ChallengeProgressResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
}
