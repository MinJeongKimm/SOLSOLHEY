package com.solsolhey.friend.entity;

import com.solsolhey.user.entity.BaseEntity;
import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 친구 관계 엔티티
 */
@Entity
@Table(name = "friends")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long friendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private User friendUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Builder
    public Friend(User user, User friendUser, FriendshipStatus status) {
        this.user = user;
        this.friendUser = friendUser;
        this.status = status != null ? status : FriendshipStatus.PENDING;
    }

    /**
     * 친구 요청 수락
     */
    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
    }

    /**
     * 친구 요청 거절
     */
    public void reject() {
        this.status = FriendshipStatus.REJECTED;
    }

    /**
     * 친구 관계 차단
     */
    public void block() {
        this.status = FriendshipStatus.BLOCKED;
    }

    public enum FriendshipStatus {
        PENDING,    // 대기 중
        ACCEPTED,   // 수락
        REJECTED,   // 거절
        BLOCKED     // 차단
    }
}
