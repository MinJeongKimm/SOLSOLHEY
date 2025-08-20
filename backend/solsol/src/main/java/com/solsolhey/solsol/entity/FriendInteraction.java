package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 간 상호작용(좋아요, 방문, 응원 등)을 기록하는 엔티티
 */
@Entity
@Table(name = "friend_interactions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_user_id", "interaction_type", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendInteraction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Long interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 상호작용을 한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser; // 상호작용을 받은 사용자

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 20)
    private InteractionType interactionType;

    @Column(name = "date", nullable = false)
    private java.sql.Date date; // 날짜별 중복 방지를 위한 날짜 필드

    @Column(name = "points_earned")
    private Integer pointsEarned; // 상호작용으로 획득한 포인트

    @Builder
    public FriendInteraction(User user, User targetUser, InteractionType interactionType, 
                           java.sql.Date date, Integer pointsEarned) {
        this.user = user;
        this.targetUser = targetUser;
        this.interactionType = interactionType;
        this.date = date;
        this.pointsEarned = pointsEarned;
    }

    /**
     * 상호작용 타입
     */
    public enum InteractionType {
        LIKE,       // 좋아요
        VISIT,      // 방문
        CHEER       // 응원
    }
}
