package com.solsolhey.challenge.dto.response;

import com.solsolhey.challenge.entity.Challenge;
import com.solsolhey.challenge.entity.UserChallenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 챌린지 상세 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeDetailResponseDto {

    // 챌린지 기본 정보
    private Long challengeId;
    private String challengeName;
    private String description;
    private Integer rewardPoints;
    private Integer rewardExp;
    private String challengeType;
    private String difficulty;
    private String categoryName;
    private String categoryDisplayName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer targetCount;
    private Boolean isActive;
    private Boolean isAvailable;

    // 통계 정보
    private Long participantCount;
    private Long completedCount;
    private Double completionRate; // 완료율

    // 현재 사용자의 참여 정보
    private Boolean isJoined;
    private UserChallengeDto userChallenge;

    public static ChallengeDetailResponseDto from(Challenge challenge) {
        return ChallengeDetailResponseDto.builder()
                .challengeId(challenge.getChallengeId())
                .challengeName(challenge.getChallengeName())
                .description(challenge.getDescription())
                .rewardPoints(challenge.getRewardPoints())
                .rewardExp(challenge.getRewardExp())
                .challengeType(challenge.getChallengeType().getDisplayName())
                .difficulty(challenge.getDifficulty().getDisplayName())
                .categoryName(challenge.getCategory().getCategoryName().name())
                .categoryDisplayName(challenge.getCategory().getDisplayName())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .targetCount(challenge.getTargetCount())
                .isActive(challenge.getIsActive())
                .isAvailable(challenge.isAvailable())
                .isJoined(false) // 기본값, 서비스에서 설정
                .build();
    }

    // 통계 정보 설정
    public void setStatistics(Long participantCount, Long completedCount) {
        this.participantCount = participantCount;
        this.completedCount = completedCount;
        if (participantCount > 0) {
            this.completionRate = (double) completedCount / participantCount;
        } else {
            this.completionRate = 0.0;
        }
    }

    // 사용자 참여 정보 설정
    public void setUserInfo(Boolean isJoined, UserChallenge userChallenge) {
        this.isJoined = isJoined;
        if (userChallenge != null) {
            this.userChallenge = UserChallengeDto.from(userChallenge);
        }
    }
}
