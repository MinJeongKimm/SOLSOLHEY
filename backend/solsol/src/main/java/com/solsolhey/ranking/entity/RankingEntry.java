package com.solsolhey.ranking.entity;

import com.solsolhey.user.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 랭킹 참가 엔티티
 * 사용자가 마스코트 스냅샷을 랭킹에 등록할 수 있음 (최대 3개)
 */
@Entity
@Table(name = "ranking_entries", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_mascot_snapshot_id", columnList = "mascot_snapshot_id"),
    @Index(name = "idx_score", columnList = "score DESC"),
    @Index(name = "idx_created_at", columnList = "created_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @NotNull(message = "사용자 ID는 필수입니다")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mascot_snapshot_id")
    private Long mascotSnapshotId = 0L; // 기본값 0으로 설정

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @NotBlank(message = "참가 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "참가 제목은 1자 이상 100자 이하여야 합니다")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(max = 500, message = "참가 설명은 500자 이하여야 합니다")
    @Column(name = "description", length = 500)
    private String description;

    @Builder
    public RankingEntry(Long userId, Long mascotSnapshotId, String title, String description, String imageUrl) {
        this.userId = userId;
        this.mascotSnapshotId = mascotSnapshotId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    /**
     * 참가 정보 수정
     */
    public void updateEntry(String title, String description) {
        this.title = title;
        this.description = description;
    }


}
