package com.solsolhey.ranking.controller;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.ranking.dto.request.CampusRankingRequest;
import com.solsolhey.ranking.dto.request.JoinRankingRequest;
import com.solsolhey.ranking.dto.request.NationalRankingRequest;
import com.solsolhey.ranking.dto.request.VoteRequest;
import com.solsolhey.ranking.dto.response.RankingResponse;
import com.solsolhey.ranking.dto.response.VoteResponse;
import com.solsolhey.ranking.entity.ContestEntry;
import com.solsolhey.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 랭킹 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ranking API", description = "교내/전국 랭킹 및 투표 관리 API")
public class RankingController {

    private final RankingService rankingService;

    /**
     * 교내 랭킹 조회
     */
    @GetMapping("/campus")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "교내 랭킹 조회", description = "캠퍼스별 마스코트 콘테스트 랭킹을 조회합니다")
    public ResponseEntity<ApiResponse<RankingResponse>> getCampusRankings(
            @Parameter(description = "캠퍼스 ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "정렬 기준 (votes_desc, trending, newest)") @RequestParam(defaultValue = "votes_desc") String sort,
            @Parameter(description = "집계 기간 (daily, weekly, monthly, all)") @RequestParam(defaultValue = "weekly") String period,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size) {

        try {
            // 요청 검증
            if (size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<RankingResponse>badRequest("페이지 크기는 1~100 범위여야 합니다."));
            }

            CampusRankingRequest request = new CampusRankingRequest(campusId, sort, period, page, size);
            String userCampus = getCurrentUserCampus();

            RankingResponse response = rankingService.getCampusRankings(request, userCampus);
            return ResponseEntity.ok(ApiResponse.success("교내 랭킹 조회 완료", response));

        } catch (IllegalArgumentException e) {
            log.warn("교내 랭킹 조회 - 잘못된 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<RankingResponse>badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("교내 랭킹 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RankingResponse>internalServerError("서버 오류가 발생했습니다."));
        }
    }

    /**
     * 전국 랭킹 조회
     */
    @GetMapping("/national")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "전국 랭킹 조회", description = "전국 마스코트 콘테스트 랭킹을 조회합니다")
    public ResponseEntity<ApiResponse<RankingResponse>> getNationalRankings(
            @Parameter(description = "정렬 기준 (votes_desc, trending, newest)") @RequestParam(defaultValue = "votes_desc") String sort,
            @Parameter(description = "집계 기간 (daily, weekly, monthly, all)") @RequestParam(defaultValue = "weekly") String period,
            @Parameter(description = "지역 필터") @RequestParam(required = false) String region,
            @Parameter(description = "학교 ID 필터") @RequestParam(required = false) Long schoolId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size) {

        try {
            // 요청 검증
            if (size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<RankingResponse>badRequest("페이지 크기는 1~100 범위여야 합니다."));
            }

            NationalRankingRequest request = new NationalRankingRequest(sort, period, region, schoolId, page, size);
            RankingResponse response = rankingService.getNationalRankings(request);
            
            return ResponseEntity.ok(ApiResponse.success("전국 랭킹 조회 완료", response));

        } catch (IllegalArgumentException e) {
            log.warn("전국 랭킹 조회 - 잘못된 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<RankingResponse>badRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("전국 랭킹 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RankingResponse>internalServerError("서버 오류가 발생했습니다."));
        }
    }

    /**
     * 교내 랭킹 투표
     */
    @PostMapping("/campus/{entryId}/vote")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "교내 랭킹 투표", description = "교내 마스코트 콘테스트에 투표합니다")
    public ResponseEntity<ApiResponse<VoteResponse>> voteForCampus(
            @Parameter(description = "투표 대상 엔트리 ID") @PathVariable Long entryId,
            @Valid @RequestBody VoteRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("잘못된 요청 데이터입니다.");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<VoteResponse>badRequest(errorMessage));
        }

        try {
            Long voterId = getCurrentUserId();
            String userCampus = getCurrentUserCampus();

            VoteResponse response = rankingService.voteForCampus(entryId, request, voterId, userCampus);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("투표가 완료되었습니다.", response));
            } else {
                // 비즈니스 로직 실패 (중복 투표, 권한 부족 등)
                if (response.getMessage().contains("중복 요청")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(ApiResponse.<VoteResponse>error(HttpStatus.CONFLICT, response.getMessage()));
                } else if (response.getMessage().contains("권한이 없습니다")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.<VoteResponse>forbidden(response.getMessage()));
                } else if (response.getMessage().contains("한도를 초과")) {
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(ApiResponse.<VoteResponse>error(HttpStatus.TOO_MANY_REQUESTS, response.getMessage()));
                } else {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.<VoteResponse>badRequest(response.getMessage()));
                }
            }

        } catch (Exception e) {
            log.error("교내 투표 처리 실패 - entryId: {}", entryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<VoteResponse>internalServerError("서버 오류가 발생했습니다."));
        }
    }

    /**
     * 전국 랭킹 투표
     */
    @PostMapping("/national/{entryId}/vote")
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "전국 랭킹 투표", description = "전국 마스코트 콘테스트에 투표합니다")
    public ResponseEntity<ApiResponse<VoteResponse>> voteForNational(
            @Parameter(description = "투표 대상 엔트리 ID") @PathVariable Long entryId,
            @Valid @RequestBody VoteRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("잘못된 요청 데이터입니다.");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<VoteResponse>badRequest(errorMessage));
        }

        try {
            Long voterId = getCurrentUserId();
            VoteResponse response = rankingService.voteForNational(entryId, request, voterId);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("투표가 완료되었습니다.", response));
            } else {
                // 비즈니스 로직 실패 처리
                if (response.getMessage().contains("중복 요청")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(ApiResponse.<VoteResponse>error(HttpStatus.CONFLICT, response.getMessage()));
                } else if (response.getMessage().contains("한도를 초과")) {
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(ApiResponse.<VoteResponse>error(HttpStatus.TOO_MANY_REQUESTS, response.getMessage()));
                } else {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.<VoteResponse>badRequest(response.getMessage()));
                }
            }

        } catch (Exception e) {
            log.error("전국 투표 처리 실패 - entryId: {}", entryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<VoteResponse>internalServerError("서버 오류가 발생했습니다."));
        }
    }

    /**
     * 교내 랭킹 참가
     */
    @PostMapping("/campus/join")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "교내 랭킹 참가", description = "마스코트를 교내 콘테스트에 참가시킵니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> joinCampusRanking(
            @Valid @RequestBody JoinRankingRequest request) {

        try {
            Long userId = getCurrentUserId();
            log.info("교내 랭킹 참가 API 호출 - 사용자 ID: {}, 마스코트 ID: {}",
                    userId, request.getMascotId());

            ContestEntry entry = rankingService.participateInContest(userId, request.getMascotId(), ContestEntry.ContestType.CAMPUS, request.getThumbnailUrl());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("entryId", entry.getEntryId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("교내 콘테스트에 성공적으로 참가했습니다.", responseData));

        } catch (BusinessException e) {
            log.warn("교내 랭킹 참가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            log.error("교내 랭킹 참가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError("콘테스트 참가에 실패했습니다."));
        }
    }

    /**
     * 전국 랭킹 참가
     */
    @PostMapping("/national/join")
    @PreAuthorize("isAuthenticated()") // 또는 특정 권한
    @Operation(summary = "전국 랭킹 참가", description = "마스코트를 전국 콘테스트에 참가시킵니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> joinNationalRanking(
            @Valid @RequestBody JoinRankingRequest request) {

        try {
            Long userId = getCurrentUserId();
            log.info("전국 랭킹 참가 API 호출 - 사용자 ID: {}, 마스코트 ID: {}",
                    userId, request.getMascotId());

            ContestEntry entry = rankingService.participateInContest(userId, request.getMascotId(), ContestEntry.ContestType.NATIONAL, request.getThumbnailUrl());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("entryId", entry.getEntryId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("전국 콘테스트에 성공적으로 참가했습니다.", responseData));

        } catch (BusinessException e) {
            log.warn("전국 랭킹 참가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            log.error("전국 랭킹 참가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError("콘테스트 참가에 실패했습니다."));
        }
    }

    // === Private Helper Methods ===

    /**
     * 현재 인증된 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        // JWT 토큰에서 사용자 ID 추출 (실제 구현에 따라 달라질 수 있음)
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("유효하지 않은 사용자 ID입니다.");
        }
    }

    /**
     * 현재 사용자의 캠퍼스 정보 조회
     */
    private String getCurrentUserCampus() {
        // 실제로는 사용자 서비스를 통해 캠퍼스 정보를 조회해야 함
        // 임시로 기본값 반환
        return "기본캠퍼스";
    }
}
