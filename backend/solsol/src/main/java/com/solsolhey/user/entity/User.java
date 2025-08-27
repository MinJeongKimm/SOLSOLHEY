package com.solsolhey.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Size(max = 50)
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
            this.totalPoints += points;
    }

    /**
     * 포인트 차감
     */
    public void deductPoints(Integer points) {
        if(points> 0)
            this.totalPoints -= points;
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
