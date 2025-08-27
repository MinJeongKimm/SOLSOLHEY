package com.solsolhey.ranking.dto.response;

import java.time.LocalDateTime;

import com.solsolhey.ranking.entity.RankingEntry;

/**
 * 랭킹 참가 정보 응답 DTO
 */
public record EntryResponse(
    Long entryId,
    Long userId,
    Long mascotSnapshotId, // null 가능
    String title,
    String description,
    String imageUrl,
    LocalDateTime createdAt
) {
    
    /**
     * RankingEntry 엔티티로부터 EntryResponse 생성
     */
    public static EntryResponse from(RankingEntry entry) {
        return new EntryResponse(
            entry.getEntryId(),
            entry.getUserId(),
            entry.getMascotSnapshotId(),
            entry.getTitle(),
            entry.getDescription(),
            entry.getImageUrl(),
            entry.getCreatedAt()
        );
    }
}
