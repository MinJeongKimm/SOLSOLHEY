package com.solsolhey.ranking.dto.response;

import com.solsolhey.ranking.entity.ContestEntry;

import lombok.Builder;
import lombok.Getter;

/**
 * 개별 랭킹 엔트리 응답 DTO
 */
@Getter
@Builder
public class RankingEntryResponse {

    private final Integer rank;              // 순위
    private final Long entryId;              // 콘테스트 엔트리 ID
    private final Long mascotId;             // 마스코트 ID
    private final String ownerNickname;      // 소유자 표시명
    private final Integer votes;             // 득표 수
    private final Double trendScore;         // 트렌딩 점수
    private final String thumbnailUrl;       // 썸네일 URL
    private final SchoolInfo school;         // 학교 정보 (전국 랭킹용)

    /**
     * ContestEntry와 추가 정보로부터 생성
     */
    public static RankingEntryResponse from(ContestEntry entry, Integer rank, 
                                          String ownerNickname, String schoolName, Long schoolId) {
        return RankingEntryResponse.builder()
                .rank(rank)
                .entryId(entry.getEntryId())
                .mascotId(entry.getMascotId())
                .ownerNickname(ownerNickname)
                .votes(entry.getVotes())
                .trendScore(entry.getTrendScore())
                .thumbnailUrl(entry.getThumbnailUrl())
                .school(schoolName != null ? new SchoolInfo(schoolId, schoolName) : null)
                .build();
    }

    /**
     * 교내 랭킹용 (학교 정보 없음)
     */
    public static RankingEntryResponse fromCampus(ContestEntry entry, Integer rank, String ownerNickname) {
        return from(entry, rank, ownerNickname, null, null);
    }

    /**
     * 전국 랭킹용 (학교 정보 포함)
     */
    public static RankingEntryResponse fromNational(ContestEntry entry, Integer rank, 
                                                   String ownerNickname, String schoolName, Long schoolId) {
        return from(entry, rank, ownerNickname, schoolName, schoolId);
    }
}
