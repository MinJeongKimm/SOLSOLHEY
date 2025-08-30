package com.solsolhey.challenge.entity;

import com.solsolhey.user.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 챌린지 엔티티
 */
@Entity
@Table(name = "challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ChallengeCategory category;

    @Column(name = "challenge_name", nullable = false, length = 100)
    private String challengeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "reward_points", nullable = false)
    private Integer rewardPoints;

    @Column(name = "reward_exp", nullable = false)
    private Integer rewardExp;

    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type", nullable = false, length = 20)
    private ChallengeType challengeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private ChallengeDifficulty difficulty;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // 리셋 정책: 일부 챌린지만 일일 리셋 적용
    public enum ResetPolicy { NONE, DAILY, WEEKLY, MONTHLY }

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_policy", nullable = false, length = 20)
    private ResetPolicy resetPolicy = ResetPolicy.NONE;

    // 리셋 기준: CALENDAR(캘린더 기준) | COMPLETION(완료일 기준)
    public enum ResetAnchor { CALENDAR, COMPLETION }

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_anchor", nullable = false, length = 20)
    private ResetAnchor resetAnchor = ResetAnchor.CALENDAR;

    @Builder
    public Challenge(ChallengeCategory category, String challengeName, String description,
                    Integer rewardPoints, Integer rewardExp, ChallengeType challengeType,
                    ChallengeDifficulty difficulty, LocalDateTime startDate, 
                    LocalDateTime endDate, Integer targetCount) {
        this.category = category;
        this.challengeName = challengeName;
        this.description = description;
        this.rewardPoints = rewardPoints;
        this.rewardExp = rewardExp;
        this.challengeType = challengeType;
        this.difficulty = difficulty;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetCount = targetCount != null ? targetCount : 1;
        this.isActive = true;
    }

    /**
     * 챌린지 활성화/비활성화
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    /**
     * 챌린지 활성 여부 확인
     */
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return this.isActive && 
               (this.startDate == null || now.isAfter(this.startDate)) &&
               (this.endDate == null || now.isBefore(this.endDate));
    }

    /**
     * 보상 업데이트
     */
    public void updateReward(Integer rewardPoints, Integer rewardExp) {
        if (rewardPoints != null && rewardPoints >= 0) {
            this.rewardPoints = rewardPoints;
        }
        if (rewardExp != null && rewardExp >= 0) {
            this.rewardExp = rewardExp;
        }
    }

    /**
     * 목표 카운트 조회
     */
    public Integer getTargetCount() {
        return this.targetCount;
    }

    /**
     * 보상 포인트 조회
     */
    public Integer getRewardPoints() {
        return this.rewardPoints;
    }

    /**
     * 보상 경험치 조회
     */
    public Integer getRewardExp() {
        return this.rewardExp;
    }

    /**
     * 챌린지 타입 열거형
     */
    public enum ChallengeType {
        DAILY("일일"),
        WEEKLY("주간"),
        MONTHLY("월간"),
        SPECIAL("특별");

        private final String displayName;

        ChallengeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 챌린지 난이도 열거형
     */
    public enum ChallengeDifficulty {
        EASY("쉬움", 1.0f),
        MEDIUM("보통", 1.5f),
        HARD("어려움", 2.0f),
        EXPERT("전문가", 3.0f);

        private final String displayName;
        private final float multiplier;

        ChallengeDifficulty(String displayName, float multiplier) {
            this.displayName = displayName;
            this.multiplier = multiplier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public float getMultiplier() {
            return multiplier;
        }
    }
}
