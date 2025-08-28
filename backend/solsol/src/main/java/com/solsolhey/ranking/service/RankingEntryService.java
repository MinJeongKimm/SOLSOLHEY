package com.solsolhey.ranking.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.solsolhey.ranking.dto.request.CreateEntryRequest;
import com.solsolhey.ranking.dto.response.EntryResponse;
import com.solsolhey.ranking.dto.response.LeaderboardResponse;

/**
 * 랭킹 참가 관련 서비스 인터페이스
 */
public interface RankingEntryService {

    /**
     * 랭킹 참가 등록
     * 
     * @param userId 사용자 ID
     * @param request 참가 등록 요청
     * @return 등록된 참가 정보
     */
    EntryResponse createEntry(Long userId, CreateEntryRequest request);

    /**
     * 사용자의 참가 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 참가 목록
     */
    List<EntryResponse> getUserEntries(Long userId);

    /**
     * 참가 취소 (삭제)
     * 
     * @param userId 사용자 ID
     * @param entryId 참가 ID
     */
    void deleteEntry(Long userId, Long entryId);

    /**
     * 전체 랭킹 보드 조회
     * 
     * @param pageable 페이지네이션 정보
     * @return 랭킹 보드
     */
    LeaderboardResponse getLeaderboard(Pageable pageable);

    /**
     * 사용자의 참가 개수 조회
     * 
     * @param userId 사용자 ID
     * @return 참가 개수
     */
    long getUserEntryCount(Long userId);

    /**
     * 사용자의 특정 타입 참가 개수 조회
     * 
     * @param userId 사용자 ID
     * @param rankingType 랭킹 타입 ("NATIONAL" 또는 "CAMPUS")
     * @return 참가 개수
     */
    long getUserEntryCountByType(Long userId, String rankingType);

    /**
     * 사용자의 특정 타입 참가 목록 조회
     * 
     * @param userId 사용자 ID
     * @param rankingType 랭킹 타입 ("NATIONAL" 또는 "CAMPUS")
     * @return 참가 목록
     */
    List<EntryResponse> getUserEntriesByType(Long userId, String rankingType);

    /**
     * 현재 사용자의 마스코트 ID 조회
     * 
     * @param userId 사용자 ID
     * @return 마스코트 ID
     */
    Long getCurrentUserMascotId(Long userId);

    /**
     * 특정 마스코트 스냅샷이 이미 참가되어 있는지 확인
     * 
     * @param mascotSnapshotId 마스코트 스냅샷 ID
     * @return 참가 여부
     */
    boolean isMascotSnapshotAlreadyParticipated(Long mascotSnapshotId);

    /**
     * 사용자가 특정 마스코트 스냅샷을 이미 참가했는지 확인
     * 
     * @param userId 사용자 ID
     * @param mascotSnapshotId 마스코트 스냅샷 ID
     * @return 참가 여부
     */
    boolean hasUserParticipatedWithSnapshot(Long userId, Long mascotSnapshotId);

    /**
     * 마스코트 이미지 업로드
     * 
     * @param mascotImage 업로드할 마스코트 이미지
     * @return 업로드된 이미지 URL
     */
    String uploadMascotImage(org.springframework.web.multipart.MultipartFile mascotImage);
}

