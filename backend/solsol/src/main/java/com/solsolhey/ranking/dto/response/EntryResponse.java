package com.solsolhey.ranking.dto.response;

import java.time.LocalDateTime;

import com.solsolhey.ranking.entity.RankingEntry;

/**
 * 랭킹 참가 정보 응답 DTO
 */
public record EntryResponse(
    Long entryId,
    Long userId,
    Long mascotSnapshotId,
    Integer score,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    /**
     * RankingEntry 엔티티로부터 EntryResponse 생성
     */
    public static EntryResponse from(RankingEntry entry) {
        return new EntryResponse(
            entry.getEntryId(),
            entry.getUserId(),
            entry.getMascotSnapshotId(),
            entry.getScore(),
            entry.getTitle(),
            entry.getDescription(),
            entry.getCreatedAt(),
            entry.getUpdatedAt()
        );
    }
}
