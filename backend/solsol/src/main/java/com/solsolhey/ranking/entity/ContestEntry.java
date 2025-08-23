package com.solsolhey.ranking.entity;

import com.solsolhey.user.entity.BaseEntity;

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
 * 콘테스트 참가 엔트리 엔티티
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

    @Column(name = "mascot_id", nullable = false)
    private Long mascotId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "contest_id")
    private Long contestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contest_type", nullable = false, length = 20)
    private ContestType contestType;

    @Column(name = "votes", nullable = false)
    private Integer votes = 0;

    @Column(name = "trend_score")
    private Double trendScore = 0.0;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EntryStatus status = EntryStatus.ACTIVE;

    /**
     * 콘테스트 타입
     */
    public enum ContestType {
        CAMPUS,    // 교내 콘테스트
        NATIONAL   // 전국 콘테스트
    }

    /**
     * 엔트리 상태
     */
    public enum EntryStatus {
        ACTIVE,        // 활성
        INACTIVE,      // 비활성
        DISQUALIFIED   // 실격
    }

    @Builder
    public ContestEntry(Long mascotId, Long userId, Long contestId, 
                       ContestType contestType, String thumbnailUrl) {
        this.mascotId = mascotId;
        this.userId = userId;
        this.contestId = contestId;
        this.contestType = contestType;
        this.thumbnailUrl = thumbnailUrl;
        this.votes = 0;
        this.trendScore = 0.0;
        this.status = EntryStatus.ACTIVE;
    }

    /**
     * 투표 수 증가
     */
    public void addVote() {
        this.votes++;
        updateTrendScore();
    }

    /**
     * 가중치 투표 추가
     */
    public void addVote(int weight) {
        this.votes += weight;
        updateTrendScore();
    }

    /**
     * 트렌드 점수 업데이트
     * 최근 투표일수록 높은 점수
     */
    private void updateTrendScore() {
        // 간단한 트렌드 점수 계산: 투표수 * 시간 가중치
        this.trendScore = this.votes * 1.0; // 추후 시간 기반 가중치 추가 가능
    }

    /**
     * 엔트리 상태 변경
     */
    public void updateStatus(EntryStatus status) {
        this.status = status;
    }

    /**
     * 썸네일 URL 업데이트
     */
    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
