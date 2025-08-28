package com.solsolhey.exp.entity;

import com.solsolhey.user.entity.BaseEntity;
import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "exp_daily_counter",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "counter_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExpDailyCounter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "counter_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "counter_date", nullable = false)
    private LocalDate counterDate; // KST LocalDate 파생 값

    @Column(name = "attendance_awarded", nullable = false)
    @Builder.Default
    private Boolean attendanceAwarded = false;

    @Column(name = "attendance_streak7_awarded", nullable = false)
    @Builder.Default
    private Boolean attendanceStreak7Awarded = false;

    @Column(name = "friend_active_award_count", nullable = false)
    @Builder.Default
    private Integer friendActiveAwardCount = 0;

    @Column(name = "friend_passive_award_count", nullable = false)
    @Builder.Default
    private Integer friendPassiveAwardCount = 0;

    @Column(name = "cat_finance_awarded", nullable = false)
    @Builder.Default
    private Boolean catFinanceAwarded = false;

    @Column(name = "cat_academic_awarded", nullable = false)
    @Builder.Default
    private Boolean catAcademicAwarded = false;

    @Column(name = "cat_social_awarded", nullable = false)
    @Builder.Default
    private Boolean catSocialAwarded = false;

    @Column(name = "cat_event_awarded", nullable = false)
    @Builder.Default
    private Boolean catEventAwarded = false;

    public void markAttendanceAwarded() { this.attendanceAwarded = true; }
    public void markStreak7Awarded() { this.attendanceStreak7Awarded = true; }
    public void incFriendActive() { this.friendActiveAwardCount = this.friendActiveAwardCount + 1; }
    public void incFriendPassive() { this.friendPassiveAwardCount = this.friendPassiveAwardCount + 1; }
    public void markFinanceAwarded() { this.catFinanceAwarded = true; }
    public void markAcademicAwarded() { this.catAcademicAwarded = true; }
    public void markSocialAwarded() { this.catSocialAwarded = true; }
    public void markEventAwarded() { this.catEventAwarded = true; }
}

