package com.solsolhey.challenge.dto.response;

import com.solsolhey.challenge.entity.UserChallenge;
import com.solsolhey.challenge.entity.UserChallengeCycle;
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

    // 주기 진행 레코드 기반 매핑 (프론트 호환을 위해 동일 필드에 매핑)
    public static UserChallengeDto fromCycle(UserChallengeCycle cycle) {
        return UserChallengeDto.builder()
                .userChallengeId(cycle.getCycleId())
                .status(cycle.getStatus().name())
                .statusDisplayName(cycle.getStatus().getDisplayName())
                .progressCount(cycle.getProgressCount())
                .targetCount(cycle.getTargetCount())
                .progressRate(cycle.getProgressRate())
                .startedAt(cycle.getStartedAt())
                .completedAt(cycle.getCompletedAt())
                .progressData(cycle.getProgressData())
                .build();
    }
}
