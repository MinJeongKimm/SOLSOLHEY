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
 * 투표 엔티티
 */
@Entity
@Table(name = "votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Column(name = "contest_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContestEntry.ContestType contestType;

    @Builder
    public Vote(Long voterId, Long entryId, ContestEntry.ContestType contestType) {
        this.voterId = voterId;
        this.entryId = entryId;
        this.contestType = contestType;
    }
}
