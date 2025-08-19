package com.solsolhey.solsol.dto.challenge;

import com.solsolhey.solsol.entity.UserChallenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 챌린지 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallengeDto {

    private Long userChallengeId;
    private String status;
    private String statusDisplayName;
    private Integer progressCount;
    private Integer targetCount;
    private Double progressRate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String progressData;

    public static UserChallengeDto from(UserChallenge userChallenge) {
        return UserChallengeDto.builder()
                .userChallengeId(userChallenge.getUserChallengeId())
                .status(userChallenge.getStatus().name())
                .statusDisplayName(userChallenge.getStatus().getDisplayName())
                .progressCount(userChallenge.getProgressCount())
                .targetCount(userChallenge.getTargetCount())
                .progressRate(userChallenge.getProgressRate())
                .startedAt(userChallenge.getStartedAt())
                .completedAt(userChallenge.getCompletedAt())
                .progressData(userChallenge.getProgressData())
                .build();
    }
}
