package com.solsolhey.ranking.entity;

import com.solsolhey.user.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 투표 기록 엔티티 (마스코트 기반)
 */
@Entity
@Table(name = "votes", indexes = {
    @Index(name = "idx_voter_mascot", columnList = "voter_id, mascot_id"),
    @Index(name = "idx_mascot_id", columnList = "mascot_id"),
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "mascot_id", nullable = false)
    private Long mascotId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 20)
    private VoteType voteType;

    @Column(name = "weight", nullable = false)
    private Integer weight = 1;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "campus_id")
    private Long campusId;

    /**
     * 투표 타입 (교내/전국)
     */
    public enum VoteType {
        CAMPUS, NATIONAL
    }

    @Builder
    public Vote(Long mascotId, Long voterId, VoteType voteType,
                Integer weight, String idempotencyKey, Long campusId) {
        this.mascotId = mascotId;
        this.voterId = voterId;
        this.voteType = voteType;
        this.weight = weight != null ? weight : 1;
        this.idempotencyKey = idempotencyKey;
        this.campusId = campusId;
    }

    /**
     * 중복 투표 체크를 위한 메서드
     */
    public boolean isSameVoter(Long voterId) {
        return this.voterId.equals(voterId);
    }

    /**
     * 같은 마스코트에 대한 투표인지 체크
     */
    public boolean isSameMascot(Long mascotId) {
        return this.mascotId.equals(mascotId);
    }

    /**
     * 멱등키가 같은지 체크
     */
    public boolean hasSameIdempotencyKey(String idempotencyKey) {
        return this.idempotencyKey != null && this.idempotencyKey.equals(idempotencyKey);
    }
}
