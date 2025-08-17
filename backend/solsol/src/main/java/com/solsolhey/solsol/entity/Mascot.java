package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마스코트 엔티티
 */
@Entity
@Table(name = "mascot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mascot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mascot_id")
    private Long mascotId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "experience_point", nullable = false)
    private Integer experiencePoint = 0;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "evolution_stage", nullable = false)
    private Integer evolutionStage = 1;

    @Column(name = "mascot_type", nullable = false, length = 50)
    private String mascotType;

    @Builder
    public Mascot(User user, String name, String mascotType) {
        this.user = user;
        this.name = name;
        this.mascotType = mascotType;
        this.experiencePoint = 0;
        this.level = 1;
        this.evolutionStage = 1;
    }

    /**
     * 경험치 추가
     */
    public void addExperience(Integer exp) {
        if (exp > 0) {
            this.experiencePoint += exp;
            checkLevelUp();
        }
    }

    /**
     * 레벨업 체크
     */
    private void checkLevelUp() {
        int newLevel = calculateLevel(this.experiencePoint);
        if (newLevel > this.level) {
            this.level = newLevel;
            checkEvolution();
        }
    }

    /**
     * 경험치를 기반으로 레벨 계산
     */
    private int calculateLevel(int exp) {
        // 레벨 계산 로직 (예: 100 exp당 1레벨)
        return Math.min((exp / 100) + 1, 100); // 최대 레벨 100
    }

    /**
     * 진화 체크
     */
    private void checkEvolution() {
        if (this.level >= 10 && this.evolutionStage == 1) {
            this.evolutionStage = 2;
        } else if (this.level >= 30 && this.evolutionStage == 2) {
            this.evolutionStage = 3;
        } else if (this.level >= 60 && this.evolutionStage == 3) {
            this.evolutionStage = 4;
        }
    }

    /**
     * 마스코트 이름 변경
     */
    public void changeName(String newName) {
        this.name = newName;
    }

    /**
     * 다음 레벨까지 필요한 경험치 계산
     */
    public int getExpToNextLevel() {
        int nextLevelExp = this.level * 100;
        return Math.max(nextLevelExp - this.experiencePoint, 0);
    }
}
