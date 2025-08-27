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
    private final String mascotName;         // 마스코트 이름
    private final String entryTitle;         // 랭킹 등록 시 설정한 제목 (우선순위)
    private final Long votes;                // 득표 수
    private final String backgroundId;       // 배경 ID (썸네일 대신)
    private final String imageUrl;           // 마스코트 스냅샷 이미지 URL (우선순위: RankingEntry > MascotSnapshot)
    private final String entryImageUrl;      // 랭킹 등록 시 업로드한 이미지 URL
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
                .mascotName(mascot.getName())
                .entryTitle(null) // 기본값
                .votes(voteCount)
                .backgroundId(mascot.getBackgroundId())
                .imageUrl(snapshot.getImageUrl())
                .entryImageUrl(null) // 기본값
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

    /**
     * RankingEntry 정보를 포함한 생성자 (이미지 등록 시)
     */
    public static RankingEntryResponse fromWithEntry(Mascot mascot, MascotSnapshot snapshot, Integer rank, 
                                                   String ownerNickname, String schoolName, Long schoolId, Long voteCount,
                                                   String entryImageUrl, String entryTitle) {
        return RankingEntryResponse.builder()
                .rank(rank)
                .mascotId(mascot.getId())
                .mascotSnapshotId(snapshot != null ? snapshot.getId() : null)
                .ownerNickname(ownerNickname)
                .mascotName(mascot.getName())
                .entryTitle(entryTitle)
                .votes(voteCount)
                .backgroundId(mascot.getBackgroundId())
                .imageUrl(snapshot != null ? snapshot.getImageUrl() : null)
                .entryImageUrl(entryImageUrl)
                .school(schoolName != null ? new SchoolInfo(schoolId, schoolName) : null)
                .build();
    }
}
