package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 카테고리 엔티티
 */
@Entity
@Table(name = "challenge_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private CategoryType categoryName;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public ChallengeCategory(CategoryType categoryName, String displayName, String description) {
        this.categoryName = categoryName;
        this.displayName = displayName;
        this.description = description;
        this.isActive = true;
    }

    /**
     * 카테고리 활성화/비활성화
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    /**
     * 챌린지 카테고리 타입 열거형
     */
    public enum CategoryType {
        FINANCE("금융"),
        ACADEMIC("학사"),
        SOCIAL("소셜"),
        EVENT("이벤트");

        private final String displayName;

        CategoryType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
