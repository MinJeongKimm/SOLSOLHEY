package com.solsolhey.friend.entity;

import com.solsolhey.user.entity.BaseEntity;
import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 친구 상호작용 엔티티 (좋아요, 응원 등)
 */
@Entity
@Table(name = "friend_interactions")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendInteraction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Long interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @Column(name = "message", length = 200)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    // 선택: 요청(예: 친구 요청)과 같은 상호작용의 원본 엔티티 ID 참조
    @Column(name = "reference_id")
    private Long referenceId;

    @Builder
    public FriendInteraction(User fromUser, User toUser, InteractionType interactionType, String message, Long referenceId) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.interactionType = interactionType;
        this.message = message;
        this.isRead = false;
        this.referenceId = referenceId;
    }

    /**
     * 상호작용 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }

    public enum InteractionType {
        LIKE,       // 좋아요
        CHEER,      // 응원
        POKE,       // 찌르기
        MESSAGE,    // 메시지
        FRIEND_REQUEST // 친구 요청 알림
    }
}
