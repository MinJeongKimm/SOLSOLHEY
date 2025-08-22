package com.solsolhey.challenge.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.challenge.dto.response.*;
import com.solsolhey.challenge.dto.request.*;
import com.solsolhey.user.entity.User;
import com.solsolhey.challenge.service.ChallengeService;
import com.solsolhey.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 챌린지 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Challenge API", description = "챌린지 관련 API")
public class ChallengeController {

    private final ChallengeService challengeService;

    /**
     * 챌린지 목록 조회
     * GET /challenges?category={category}
     */
    @GetMapping
    @Operation(summary = "챌린지 목록 조회", description = "카테고리별로 챌린지 목록을 조회합니다")
    public ResponseEntity<List<ChallengeListResponseDto>> getChallenges(
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("챌린지 목록 조회 요청: category={}, userId={}", category, userDetails.getUserId());
        
        User currentUser = userDetails.getUser();
        List<ChallengeListResponseDto> challenges = challengeService.getChallenges(category, currentUser);
        
        return ResponseEntity.ok(challenges);
    }

    /**
     * 챌린지 상세 조회
     * GET /challenges/{challengeId}
     */
    @GetMapping("/{challengeId}")
    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다")
    public ResponseEntity<ChallengeDetailResponseDto> getChallengeDetail(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("챌린지 상세 조회 요청: challengeId={}, userId={}", challengeId, userDetails.getUserId());
        
        User currentUser = userDetails.getUser();
        ChallengeDetailResponseDto challenge = challengeService.getChallengeDetail(challengeId, currentUser);
        
        return ResponseEntity.ok(challenge);
    }

    /**
     * 챌린지 참여
     * POST /challenges/{challengeId}/join
     */
    @PostMapping("/{challengeId}/join")
    @Operation(summary = "챌린지 참여", description = "사용자가 특정 챌린지에 참여합니다")
    public ResponseEntity<ChallengeJoinResponseDto> joinChallenge(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("챌린지 참여 요청: challengeId={}, userId={}", challengeId, userDetails.getUserId());
        
        User currentUser = userDetails.getUser();
        ChallengeJoinResponseDto response = challengeService.joinChallenge(challengeId, currentUser);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 챌린지 진행도 갱신
     * POST /challenges/{challengeId}/progress
     */
    @PostMapping("/{challengeId}/progress")
    @Operation(summary = "챌린지 진행도 갱신", description = "사용자의 챌린지 진행도를 업데이트합니다")
    public ResponseEntity<ChallengeProgressResponseDto> updateProgress(
            @PathVariable Long challengeId,
            @Valid @RequestBody ChallengeProgressRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("챌린지 진행도 갱신 요청: challengeId={}, userId={}, step={}", 
                challengeId, userDetails.getUserId(), request.getStep());
        
        User currentUser = userDetails.getUser();
        ChallengeProgressResponseDto response = challengeService.updateProgress(challengeId, request, currentUser);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자의 챌린지 참여 내역 조회
     * GET /challenges/my?status={status}
     */
    @GetMapping("/my")
    @Operation(summary = "내 챌린지 조회", description = "현재 사용자가 참여한 챌린지 목록을 조회합니다")
    public ResponseEntity<List<UserChallengeDto>> getMyChallenges(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("사용자 챌린지 내역 조회 요청: userId={}, status={}", userDetails.getUserId(), status);
        
        User currentUser = userDetails.getUser();
        List<UserChallengeDto> challenges = challengeService.getUserChallenges(currentUser, status);
        
        return ResponseEntity.ok(challenges);
    }
}
