package com.solsolhey.ranking.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.solsolhey.ranking.entity.RankingEntry;

/**
 * 랭킹 보드 응답 DTO
 */
public record LeaderboardResponse(
    List<LeaderboardEntry> entries,
    int currentPage,
    int totalPages,
    long totalElements,
    boolean hasNext,
    boolean hasPrevious
) {
    
    /**
     * Page<RankingEntry>로부터 LeaderboardResponse 생성
     */
    public static LeaderboardResponse from(Page<RankingEntry> page) {
        List<LeaderboardEntry> entries = page.getContent().stream()
            .map(LeaderboardEntry::from)
            .toList();
            
        return new LeaderboardResponse(
            entries,
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
    
    /**
     * 랭킹 보드의 개별 항목 DTO
     */
    public record LeaderboardEntry(
        Long entryId,
        Long userId,
        Long mascotSnapshotId,
        String title,
        String description,
        String createdAt,
        int rank,
        long voteCount
    ) {
        
        /**
         * RankingEntry 엔티티로부터 LeaderboardEntry 생성
         */
        public static LeaderboardEntry from(RankingEntry entry) {
            return new LeaderboardEntry(
                entry.getEntryId(),
                entry.getUserId(),
                entry.getMascotSnapshotId(),
                entry.getTitle(),
                entry.getDescription(),
                entry.getCreatedAt().toString(),
                0, // rank는 서비스에서 계산
                0  // voteCount는 서비스에서 계산
            );
        }
        
        /**
         * 순위와 투표수 설정
         */
        public LeaderboardEntry withRankAndVoteCount(int rank, long voteCount) {
            return new LeaderboardEntry(
                this.entryId,
                this.userId,
                this.mascotSnapshotId,
                this.title,
                this.description,
                this.createdAt,
                rank,
                voteCount
            );
        }
    }
}
