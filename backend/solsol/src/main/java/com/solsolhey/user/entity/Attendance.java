package com.solsolhey.user.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출석 기록 엔티티
 */
@Entity
@Table(name = "attendance", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "attendance_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "consecutive_days", nullable = false)
    private Integer consecutiveDays = 1;

    @Column(name = "exp_reward", nullable = false)
    private Integer expReward = 0;

    @Column(name = "point_reward", nullable = false)
    private Integer pointReward = 0;

    @Builder
    public Attendance(User user, LocalDate attendanceDate, Integer consecutiveDays,
                     Integer expReward, Integer pointReward) {
        this.user = user;
        this.attendanceDate = attendanceDate != null ? attendanceDate : LocalDate.now();
        this.consecutiveDays = consecutiveDays != null ? consecutiveDays : 1;
        this.expReward = expReward != null ? expReward : calculateExpReward(this.consecutiveDays);
        this.pointReward = pointReward != null ? pointReward : calculatePointReward(this.consecutiveDays);
    }

    /**
     * 연속 출석 일수 기반 경험치 보상 계산
     */
    private Integer calculateExpReward(Integer consecutiveDays) {
        int baseExp = 5; // 기본 경험치
        
        // 연속 출석 보너스
        if (consecutiveDays >= 7) {
            baseExp += 15; // 7일 연속 보너스
        }
        if (consecutiveDays >= 30) {
            baseExp += 30; // 30일 연속 보너스
        }
        
        return baseExp;
    }

    /**
     * 연속 출석 일수 기반 포인트 보상 계산
     */
    private Integer calculatePointReward(Integer consecutiveDays) {
        int basePoints = 10; // 기본 포인트
        
        // 연속 출석 보너스
        if (consecutiveDays >= 7) {
            basePoints += 20; // 7일 연속 보너스
        }
        if (consecutiveDays >= 30) {
            basePoints += 50; // 30일 연속 보너스
        }
        
        return basePoints;
    }

    /**
     * 연속 출석 일수 업데이트
     */
    public void updateConsecutiveDays(Integer consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
        this.expReward = calculateExpReward(consecutiveDays);
        this.pointReward = calculatePointReward(consecutiveDays);
    }

    /**
     * 특별 보상 적용 (이벤트 등)
     */
    public void applySpecialReward(Integer bonusExp, Integer bonusPoints) {
        if (bonusExp != null && bonusExp > 0) {
            this.expReward += bonusExp;
        }
        if (bonusPoints != null && bonusPoints > 0) {
            this.pointReward += bonusPoints;
        }
    }

    /**
     * 주말 여부 확인
     */
    public boolean isWeekend() {
        return attendanceDate.getDayOfWeek().getValue() >= 6; // 토요일(6), 일요일(7)
    }

    /**
     * 이번달 출석인지 확인
     */
    public boolean isThisMonth() {
        return attendanceDate.getMonthValue() == LocalDate.now().getMonthValue() &&
               attendanceDate.getYear() == LocalDate.now().getYear();
    }
}
