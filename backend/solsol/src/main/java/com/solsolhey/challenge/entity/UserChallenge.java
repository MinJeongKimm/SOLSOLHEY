package com.solsolhey.challenge.entity;

import com.solsolhey.user.entity.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자-챌린지 참여 엔티티
 */
@Entity
@Table(name = "user_challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_challenge_id")
    private Long userChallengeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChallengeStatus status = ChallengeStatus.NOT_STARTED;

    @Column(name = "progress_count", nullable = false)
    private Integer progressCount = 0;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "progress_data", columnDefinition = "TEXT")
    private String progressData; // JSON 형태로 추가 진행 데이터 저장

    @Builder
    public UserChallenge(User user, Challenge challenge) {
        this.user = user;
        this.challenge = challenge;
        this.status = ChallengeStatus.NOT_STARTED;
        this.progressCount = 0;
        this.targetCount = challenge.getTargetCount();
    }

    /**
     * 챌린지 시작
     */
    public void start() {
        if (this.status == ChallengeStatus.NOT_STARTED) {
            this.status = ChallengeStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
        }
    }

    /**
     * 진행도 업데이트
     */
    public void updateProgress(Integer progress) {
        if (this.status == ChallengeStatus.IN_PROGRESS) {
            this.progressCount = Math.min(progress, this.targetCount);
            
            if (this.progressCount >= this.targetCount) {
                complete();
            }
        }
    }

    /**
     * 진행도 증가
     */
    public void incrementProgress() {
        updateProgress(this.progressCount + 1);
    }

    /**
     * 챌린지 완료
     */
    public void complete() {
        if (this.status == ChallengeStatus.IN_PROGRESS) {
            this.status = ChallengeStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
            
            // 보상 지급
            this.user.addPoints(this.challenge.getRewardPoints());
            // 마스코트가 있다면 경험치 추가 (추후 구현)
        }
    }

    /**
     * 챌린지 실패
     */
    public void fail() {
        if (this.status == ChallengeStatus.IN_PROGRESS) {
            this.status = ChallengeStatus.FAILED;
        }
    }

    /**
     * 챌린지 만료
     */
    public void expire() {
        if (this.status == ChallengeStatus.IN_PROGRESS || this.status == ChallengeStatus.NOT_STARTED) {
            this.status = ChallengeStatus.EXPIRED;
        }
    }

    /**
     * 진행률 계산 (0.0 ~ 1.0)
     */
    public double getProgressRate() {
        if (this.targetCount == 0) return 0.0;
        return Math.min(1.0, (double) this.progressCount / this.targetCount);
    }

    /**
     * 완료 여부 확인
     */
    public boolean isCompleted() {
        return this.status == ChallengeStatus.COMPLETED;
    }

    /**
     * 진행 중 여부 확인
     */
    public boolean isInProgress() {
        return this.status == ChallengeStatus.IN_PROGRESS;
    }

    /**
     * 추가 진행 데이터 업데이트
     */
    public void updateProgressData(String progressData) {
        this.progressData = progressData;
    }

    /**
     * 챌린지 상태 열거형
     */
    public enum ChallengeStatus {
        NOT_STARTED("시작 전"),
        IN_PROGRESS("진행 중"),
        COMPLETED("완료"),
        FAILED("실패"),
        EXPIRED("만료");

        private final String displayName;

        ChallengeStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
