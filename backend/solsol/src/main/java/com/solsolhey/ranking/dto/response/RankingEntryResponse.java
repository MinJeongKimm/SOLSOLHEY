package com.solsolhey.ranking.dto.response;

import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.domain.MascotSnapshot;

import lombok.Builder;
import lombok.Getter;

/**
 * 개별 랭킹 엔트리 응답 DTO (마스코트 기반)
 */
@Getter
@Builder
public class RankingEntryResponse {

    private final Integer rank;              // 순위
    private final Long mascotId;             // 마스코트 ID
    private final Long mascotSnapshotId;     // 마스코트 스냅샷 ID
    private final String ownerNickname;      // 소유자 표시명
    private final Long votes;                // 득표 수
    private final String backgroundId;       // 배경 ID (썸네일 대신)
    private final String imageUrl;           // 마스코트 스냅샷 이미지 URL
    private final SchoolInfo school;         // 학교 정보 (전국 랭킹용)

    /**
     * Mascot, MascotSnapshot과 추가 정보로부터 생성
     */
    public static RankingEntryResponse from(Mascot mascot, MascotSnapshot snapshot, Integer rank, 
                                          String ownerNickname, String schoolName, Long schoolId, Long voteCount) {
        return RankingEntryResponse.builder()
                .rank(rank)
                .mascotId(mascot.getId())
                .mascotSnapshotId(snapshot.getId())
                .ownerNickname(ownerNickname)
                .votes(voteCount)
                .backgroundId(mascot.getBackgroundId())
                .imageUrl(snapshot.getImageUrl())
                .school(schoolName != null ? new SchoolInfo(schoolId, schoolName) : null)
                .build();
    }

    /**
     * 교내 랭킹용 (학교 정보 없음)
     */
    public static RankingEntryResponse fromCampus(Mascot mascot, MascotSnapshot snapshot, Integer rank, String ownerNickname, Long voteCount) {
        return from(mascot, snapshot, rank, ownerNickname, null, null, voteCount);
    }

    /**
     * 전국 랭킹용 (학교 정보 포함)
     */
    public static RankingEntryResponse fromNational(Mascot mascot, MascotSnapshot snapshot, Integer rank, 
                                                   String ownerNickname, String schoolName, Long schoolId, Long voteCount) {
        return from(mascot, snapshot, rank, ownerNickname, schoolName, schoolId, voteCount);
    }
}
