package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "campus", length = 100)
    private String campus;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public User(String username, String email, String passwordHash, 
                String nickname, String campus) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.campus = campus;
        this.totalPoints = 0;
        this.isActive = true;
    }

    /**
     * 포인트 추가
     */
    public void addPoints(Integer points) {
        if (points > 0) {
            this.totalPoints += points;
        }
    }

    /**
     * 포인트 차감
     */
    public void deductPoints(Integer points) {
        if (points > 0 && this.totalPoints >= points) {
            this.totalPoints -= points;
        } else {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }

    /**
     * 닉네임 변경
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.isActive = true;
    }
}
