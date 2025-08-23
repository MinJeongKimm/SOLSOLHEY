package com.solsolhey.ranking.entity;

import com.solsolhey.user.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 투표 기록 엔티티
 */
@Entity
@Table(name = "votes", indexes = {
    @Index(name = "idx_voter_entry", columnList = "voter_id, entry_id"),
    @Index(name = "idx_entry_id", columnList = "entry_id"),
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contest_type", nullable = false, length = 20)
    private ContestEntry.ContestType contestType;

    @Column(name = "weight", nullable = false)
    private Integer weight = 1;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "campus_id")
    private Long campusId;

    @Column(name = "contest_id")
    private Long contestId;

    @Builder
    public Vote(Long entryId, Long voterId, ContestEntry.ContestType contestType,
                Integer weight, String idempotencyKey, Long campusId, Long contestId) {
        this.entryId = entryId;
        this.voterId = voterId;
        this.contestType = contestType;
        this.weight = weight != null ? weight : 1;
        this.idempotencyKey = idempotencyKey;
        this.campusId = campusId;
        this.contestId = contestId;
    }

    /**
     * 중복 투표 체크를 위한 메서드
     */
    public boolean isSameVoter(Long voterId) {
        return this.voterId.equals(voterId);
    }

    /**
     * 같은 엔트리에 대한 투표인지 체크
     */
    public boolean isSameEntry(Long entryId) {
        return this.entryId.equals(entryId);
    }

    /**
     * 멱등키가 같은지 체크
     */
    public boolean hasSameIdempotencyKey(String idempotencyKey) {
        return this.idempotencyKey != null && this.idempotencyKey.equals(idempotencyKey);
    }
}
