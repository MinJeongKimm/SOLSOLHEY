package com.solsolhey.ranking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.ranking.dto.request.CreateEntryRequest;
import com.solsolhey.ranking.dto.response.EntryResponse;
import com.solsolhey.ranking.dto.response.LeaderboardResponse;
import com.solsolhey.ranking.service.RankingEntryService;

import jakarta.validation.Valid;

/**
 * 랭킹 참가 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/ranking/entries")
public class RankingEntryController {

    @Autowired
    private RankingEntryService rankingEntryService;

    /**
     * 랭킹 참가 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EntryResponse>> createEntry(
            @Valid @RequestBody CreateEntryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUserId();
            EntryResponse response = rankingEntryService.createEntry(userId, request);
            
            return ResponseEntity.ok(ApiResponse.success("랭킹 참가가 성공적으로 등록되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("랭킹 참가 등록에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자의 참가 목록 조회
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<EntryResponse>>> getUserEntries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUserId();
            List<EntryResponse> entries = rankingEntryService.getUserEntries(userId);
            
            return ResponseEntity.ok(ApiResponse.success("사용자 참가 목록을 성공적으로 조회했습니다.", entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("참가 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 사용자의 참가 목록 조회 (관리자용)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<EntryResponse>>> getUserEntriesByUserId(
            @PathVariable Long userId) {
        try {
            List<EntryResponse> entries = rankingEntryService.getUserEntries(userId);
            
            return ResponseEntity.ok(ApiResponse.success("사용자 참가 목록을 성공적으로 조회했습니다.", entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("참가 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 참가 취소 (삭제)
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUserId();
            rankingEntryService.deleteEntry(userId, entryId);
            
            return ResponseEntity.ok(ApiResponse.success("참가가 성공적으로 취소되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("참가 취소에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 전체 랭킹 보드 조회
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            LeaderboardResponse leaderboard = rankingEntryService.getLeaderboard(pageable);
            
            return ResponseEntity.ok(ApiResponse.success("랭킹 보드를 성공적으로 조회했습니다.", leaderboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("랭킹 보드 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자의 참가 개수 조회
     */
    @GetMapping("/user/count")
    public ResponseEntity<ApiResponse<Long>> getUserEntryCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUserId();
            long count = rankingEntryService.getUserEntryCount(userId);
            
            return ResponseEntity.ok(ApiResponse.success("사용자 참가 개수를 성공적으로 조회했습니다.", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("참가 개수 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}
