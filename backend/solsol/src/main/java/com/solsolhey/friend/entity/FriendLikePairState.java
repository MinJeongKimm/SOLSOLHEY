package com.solsolhey.friend.entity;

import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friend_like_pair_state",
       uniqueConstraints = {@UniqueConstraint(name = "uk_like_pair_low_high", columnNames = {"user_low_id", "user_high_id"})})
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendLikePairState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "state_id")
    private Long stateId;

    // 항상 userLowId < userHighId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_low_id", nullable = false)
    private User userLow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_high_id", nullable = false)
    private User userHigh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_sender_id")
    private User lastSender; // 마지막 LIKE 발신자

    public FriendLikePairState(User low, User high) {
        this.userLow = low;
        this.userHigh = high;
    }
}

