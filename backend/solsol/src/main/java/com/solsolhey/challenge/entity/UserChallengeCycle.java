package com.solsolhey.challenge.entity;

import com.solsolhey.user.entity.BaseEntity;
import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자-챌린지 주기별 진행 엔티티 (KST LocalDate 기준)
 * cycleType: DAILY | WEEKLY | MONTHLY
 * cycleKeyDate: 해당 주기의 시작일(LocalDate)
 */
@Entity
@Table(name = "user_challenge_cycle",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id", "cycle_type", "cycle_key_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChallengeCycle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cycle_id")
    private Long cycleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_type", nullable = false, length = 20)
    private Challenge.ResetPolicy cycleType;

    @Column(name = "cycle_key_date", nullable = false)
    private LocalDate cycleKeyDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserChallenge.ChallengeStatus status = UserChallenge.ChallengeStatus.NOT_STARTED;

    @Column(name = "progress_count", nullable = false)
    private Integer progressCount = 0;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "progress_data", columnDefinition = "TEXT")
    private String progressData;

    @Builder
    public UserChallengeCycle(User user, Challenge challenge, Challenge.ResetPolicy cycleType, LocalDate cycleKeyDate) {
        this.user = user;
        this.challenge = challenge;
        this.cycleType = cycleType;
        this.cycleKeyDate = cycleKeyDate;
        this.status = UserChallenge.ChallengeStatus.NOT_STARTED;
        this.progressCount = 0;
        this.targetCount = challenge.getTargetCount();
    }

    public void start() {
        if (this.status == UserChallenge.ChallengeStatus.NOT_STARTED) {
            this.status = UserChallenge.ChallengeStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
        }
    }

    public void updateProgress(Integer progress) {
        if (this.status == UserChallenge.ChallengeStatus.IN_PROGRESS) {
            this.progressCount = Math.min(progress, this.targetCount);
            if (this.progressCount >= this.targetCount) {
                complete();
            }
        }
    }

    public void incrementProgress() { updateProgress(this.progressCount + 1); }

    public void complete() {
        if (this.status == UserChallenge.ChallengeStatus.IN_PROGRESS) {
            this.status = UserChallenge.ChallengeStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    public void fail() {
        if (this.status == UserChallenge.ChallengeStatus.IN_PROGRESS) {
            this.status = UserChallenge.ChallengeStatus.FAILED;
        }
    }

    public void expire() {
        if (this.status == UserChallenge.ChallengeStatus.IN_PROGRESS || this.status == UserChallenge.ChallengeStatus.NOT_STARTED) {
            this.status = UserChallenge.ChallengeStatus.EXPIRED;
        }
    }

    public double getProgressRate() {
        if (this.targetCount == 0) return 0.0;
        return Math.min(1.0, (double) this.progressCount / this.targetCount);
    }

    public boolean isCompleted() { return this.status == UserChallenge.ChallengeStatus.COMPLETED; }
    public boolean isInProgress() { return this.status == UserChallenge.ChallengeStatus.IN_PROGRESS; }
    public void updateProgressData(String progressData) { this.progressData = progressData; }
}

