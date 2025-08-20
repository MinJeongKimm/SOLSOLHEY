package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 관계를 나타내는 엔티티
 * 양방향 관계가 아닌 단방향 관계로 구현
 */
@Entity
@Table(name = "friends", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long friendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 친구를 추가한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private User friendUser; // 친구로 추가된 사용자

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FriendStatus status = FriendStatus.ACTIVE;

    @Builder
    public Friend(User user, User friendUser) {
        this.user = user;
        this.friendUser = friendUser;
        this.status = FriendStatus.ACTIVE;
    }

    /**
     * 친구 관계 상태
     */
    public enum FriendStatus {
        ACTIVE, // 활성 상태
        BLOCKED // 차단 상태
    }

    /**
     * 친구 관계 차단
     */
    public void block() {
        this.status = FriendStatus.BLOCKED;
    }

    /**
     * 친구 관계 차단 해제
     */
    public void unblock() {
        this.status = FriendStatus.ACTIVE;
    }

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return this.status == FriendStatus.ACTIVE;
    }
}
