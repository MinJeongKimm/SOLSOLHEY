package com.solsolhey.ranking.entity;

import java.time.LocalDateTime;

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
 * 콘테스트 정보 엔티티
 */
@Entity
@Table(name = "contests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_id")
    private Long contestId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ContestEntry.ContestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContestStatus status = ContestStatus.PREPARING;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "max_entries")
    private Integer maxEntries;

    @Column(name = "campus_id")
    private Long campusId; // 교내 콘테스트의 경우 캠퍼스 ID

    @Column(name = "region", length = 50)
    private String region; // 전국 콘테스트의 경우 지역 구분

    /**
     * 콘테스트 상태
     */
    public enum ContestStatus {
        PREPARING,  // 준비중
        ACTIVE,     // 진행중
        ENDED,      // 종료
        CANCELLED   // 취소
    }

    @Builder
    public Contest(String name, ContestEntry.ContestType type, String description,
                   LocalDateTime startDate, LocalDateTime endDate, Integer maxEntries,
                   Long campusId, String region) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxEntries = maxEntries;
        this.campusId = campusId;
        this.region = region;
        this.status = ContestStatus.PREPARING;
    }

    /**
     * 콘테스트 시작
     */
    public void start() {
        if (this.status == ContestStatus.PREPARING) {
            this.status = ContestStatus.ACTIVE;
        }
    }

    /**
     * 콘테스트 종료
     */
    public void end() {
        if (this.status == ContestStatus.ACTIVE) {
            this.status = ContestStatus.ENDED;
        }
    }

    /**
     * 콘테스트 취소
     */
    public void cancel() {
        if (this.status == ContestStatus.PREPARING || this.status == ContestStatus.ACTIVE) {
            this.status = ContestStatus.CANCELLED;
        }
    }

    /**
     * 콘테스트가 활성 상태인지 확인
     */
    public boolean isActive() {
        return this.status == ContestStatus.ACTIVE && 
               LocalDateTime.now().isAfter(startDate) && 
               LocalDateTime.now().isBefore(endDate);
    }

    /**
     * 콘테스트가 종료되었는지 확인
     */
    public boolean isEnded() {
        return this.status == ContestStatus.ENDED || 
               LocalDateTime.now().isAfter(endDate);
    }

    /**
     * 참가 가능한지 확인
     */
    public boolean canParticipate() {
        return this.status == ContestStatus.ACTIVE && 
               LocalDateTime.now().isAfter(startDate) && 
               LocalDateTime.now().isBefore(endDate);
    }

    /**
     * 투표 가능한지 확인
     */
    public boolean canVote() {
        return canParticipate(); // 참가 가능할 때 투표도 가능
    }
}
