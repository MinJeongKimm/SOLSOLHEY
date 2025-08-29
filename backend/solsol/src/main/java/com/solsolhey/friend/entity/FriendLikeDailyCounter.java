package com.solsolhey.friend.entity;

import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "friend_like_daily_counter",
       uniqueConstraints = {@UniqueConstraint(name = "uk_like_counter_from_to_date", columnNames = {"from_user_id", "to_user_id", "like_date"})})
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendLikeDailyCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "counter_id")
    private Long counterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(name = "like_date", nullable = false)
    private LocalDate likeDate;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    public FriendLikeDailyCounter(User fromUser, User toUser, LocalDate likeDate) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.likeDate = likeDate;
        this.likeCount = 0;
    }

    public void inc() { this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1; }
}

