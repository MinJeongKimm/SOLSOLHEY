package com.solsolhey.ranking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.ranking.dto.request.CampusRankingRequest;
import com.solsolhey.ranking.dto.request.NationalRankingRequest;
import com.solsolhey.ranking.dto.request.VoteRequest;
import com.solsolhey.ranking.dto.response.RankingResponse;
import com.solsolhey.ranking.dto.response.VoteResponse;
import com.solsolhey.ranking.service.RankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 랭킹 API 컨트롤러 (마스코트 기반)
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
    @Operation(summary = "교내 랭킹 조회", description = "캠퍼스별 마스코트 랭킹을 조회합니다")
    public ResponseEntity<ApiResponse<RankingResponse>> getCampusRankings(
            @Parameter(description = "캠퍼스 ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "정렬 기준 (votes_desc, trending, newest)") @RequestParam(defaultValue = "votes_desc") String sort,
            @Parameter(description = "집계 기간 (daily, weekly, monthly, all)") @RequestParam(defaultValue = "weekly") String period,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.solsolhey.auth.dto.response.CustomUserDetails userDetails) {

        try {
            // 요청 검증
            if (size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<RankingResponse>badRequest("페이지 크기는 1~100 범위여야 합니다."));
            }

            CampusRankingRequest request = new CampusRankingRequest(campusId, sort, period, page, size);
            String userCampus = userDetails.getUser().getCampus();

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
    @Operation(summary = "전국 랭킹 조회", description = "전국 마스코트 랭킹을 조회합니다")
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
    @Operation(summary = "교내 랭킹 투표", description = "교내 랭킹 엔트리에 투표합니다")
    public ResponseEntity<ApiResponse<VoteResponse>> voteForCampus(
            @Parameter(description = "투표 대상 랭킹 엔트리 ID") @PathVariable Long entryId,
            @Valid @RequestBody VoteRequest request,
            BindingResult bindingResult,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.solsolhey.auth.dto.response.CustomUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("잘못된 요청 데이터입니다.");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<VoteResponse>badRequest(errorMessage));
        }

        try {
            Long voterId = userDetails.getUser().getUserId();
            String userCampus = userDetails.getUser().getCampus();

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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "전국 랭킹 투표", description = "전국 랭킹 엔트리에 투표합니다")
    public ResponseEntity<ApiResponse<VoteResponse>> voteForNational(
            @Parameter(description = "투표 대상 랭킹 엔트리 ID") @PathVariable Long entryId,
            @Valid @RequestBody VoteRequest request,
            BindingResult bindingResult,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.solsolhey.auth.dto.response.CustomUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("잘못된 요청 데이터입니다.");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<VoteResponse>badRequest(errorMessage));
        }

        try {
            Long voterId = userDetails.getUser().getUserId();
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
     * 사용자 투표 히스토리 조회
     */
    @GetMapping("/campus/my-votes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 교내 랭킹 투표 히스토리 조회", description = "현재 로그인한 사용자가 교내 랭킹에 투표한 마스코트 ID 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<Long>>> getMyCampusVotedMascotIds(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.solsolhey.auth.dto.response.CustomUserDetails userDetails) {

        try {
            Long voterId = userDetails.getUser().getUserId();
            List<Long> votedMascotIds = rankingService.getUserVotedMascotIdsForCampus(voterId);
            
            return ResponseEntity.ok(ApiResponse.success("교내 랭킹 투표 히스토리 조회 완료", votedMascotIds));

        } catch (Exception e) {
            log.error("사용자 교내 랭킹 투표 히스토리 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<Long>>internalServerError("교내 랭킹 투표 히스토리를 조회하는데 실패했습니다."));
        }
    }

    /**
     * 사용자 전국 랭킹 투표 히스토리 조회
     */
    @GetMapping("/national/my-votes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 전국 랭킹 투표 히스토리 조회", description = "현재 로그인한 사용자가 전국 랭킹에 투표한 마스코트 ID 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<Long>>> getMyNationalVotedMascotIds(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.solsolhey.auth.dto.response.CustomUserDetails userDetails) {

        try {
            Long voterId = userDetails.getUser().getUserId();
            List<Long> votedMascotIds = rankingService.getUserVotedMascotIdsForNational(voterId);
            
            return ResponseEntity.ok(ApiResponse.success("전국 랭킹 투표 히스토리 조회 완료", votedMascotIds));

        } catch (Exception e) {
            log.error("사용자 전국 랭킹 투표 히스토리 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<Long>>internalServerError("전국 랭킹 투표 히스토리를 조회하는데 실패했습니다."));
        }
    }

}
