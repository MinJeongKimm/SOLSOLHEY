package com.solsolhey.challenge.dto.response;

import com.solsolhey.challenge.entity.Challenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 챌린지 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeListResponseDto {

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

    // 참여 관련 정보
    private Long participantCount;
    private Long completedCount;
    private Boolean isJoined; // 현재 사용자가 참여했는지 여부
    private String userStatus; // 현재 사용자의 참여 상태

    public static ChallengeListResponseDto from(Challenge challenge) {
        return ChallengeListResponseDto.builder()
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
                .userStatus("NOT_JOINED") // 기본값, 서비스에서 설정
                .build();
    }

    public static List<ChallengeListResponseDto> fromList(List<Challenge> challenges) {
        return challenges.stream()
                .map(ChallengeListResponseDto::from)
                .collect(Collectors.toList());
    }

    // 참여 정보 설정 메서드들
    public void setParticipantInfo(Long participantCount, Long completedCount) {
        this.participantCount = participantCount;
        this.completedCount = completedCount;
    }

    public void setUserInfo(Boolean isJoined, String userStatus) {
        this.isJoined = isJoined;
        this.userStatus = userStatus;
    }
}
