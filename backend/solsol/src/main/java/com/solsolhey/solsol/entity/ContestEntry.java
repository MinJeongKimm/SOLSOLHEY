package com.solsolhey.solsol.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 콘테스트 참가 엔티티
 */
@Entity
@Table(name = "contest_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mascot_id", nullable = false)
    private Long mascotId;

    @Column(name = "contest_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContestType contestType;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EntryStatus status;

    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Column(name = "vote_count", nullable = false)
    private Integer voteCount = 0;

    @Builder
    public ContestEntry(Long userId, Long mascotId, ContestType contestType) {
        this.userId = userId;
        this.mascotId = mascotId;
        this.contestType = contestType;
        this.status = EntryStatus.ACTIVE;
        this.score = 0;
        this.voteCount = 0;
    }

    /**
     * 점수 업데이트
     */
    public void updateScore(Integer newScore) {
        this.score = newScore;
    }

    /**
     * 투표 수 증가
     */
    public void addVote() {
        this.voteCount++;
        // 투표 수에 따른 점수 계산 (예: 투표 1개당 10점)
        this.score = this.voteCount * 10;
    }

    /**
     * 상태 변경
     */
    public void updateStatus(EntryStatus status) {
        this.status = status;
    }

    public enum ContestType {
        CAMPUS,     // 교내
        NATIONAL    // 전국
    }

    public enum EntryStatus {
        ACTIVE,     // 활성
        INACTIVE,   // 비활성
        DISQUALIFIED // 실격
    }
}
