package com.solsolhey.solsol.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.solsol.common.response.ApiResponse;
import com.solsolhey.solsol.dto.ranking.RankingResponseDto;
import com.solsolhey.solsol.dto.ranking.VoteRequestDto;
import com.solsolhey.solsol.dto.ranking.VoteResponseDto;
import com.solsolhey.solsol.service.RankingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 랭킹 컨트롤러
 */
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    /**
     * 교내 랭킹 조회
     */
    @GetMapping("/campus")
    public ResponseEntity<ApiResponse<List<RankingResponseDto>>> getCampusRankings(
            @RequestParam(value = "sort", defaultValue = "score") String sort) {
        
        log.info("교내 랭킹 조회 요청 - 정렬: {}", sort);
        
        try {
            List<RankingResponseDto> rankings = rankingService.getCampusRankings(sort);
            return ResponseEntity.ok(ApiResponse.success("교내 랭킹 조회 성공", rankings));
        } catch (Exception e) {
            log.error("교내 랭킹 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("교내 랭킹 조회에 실패했습니다."));
        }
    }

    /**
     * 전국 랭킹 조회
     */
    @GetMapping("/national")
    public ResponseEntity<ApiResponse<List<RankingResponseDto>>> getNationalRankings(
            @RequestParam(value = "sort", defaultValue = "score") String sort) {
        
        log.info("전국 랭킹 조회 요청 - 정렬: {}", sort);
        
        try {
            List<RankingResponseDto> rankings = rankingService.getNationalRankings(sort);
            return ResponseEntity.ok(ApiResponse.success("전국 랭킹 조회 성공", rankings));
        } catch (Exception e) {
            log.error("전국 랭킹 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("전국 랭킹 조회에 실패했습니다."));
        }
    }

    /**
     * 전국 랭킹 투표
     */
    @PostMapping("/national/{entryId}/vote")
    public ResponseEntity<ApiResponse<VoteResponseDto>> voteForNationalRanking(
            @PathVariable Long entryId,
            @RequestBody VoteRequestDto voteRequest) {
        
        log.info("전국 랭킹 투표 요청 - entryId: {}, voteRequest: {}", entryId, voteRequest);
        
        try {
            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long voterId = Long.parseLong(authentication.getName());
            
            VoteResponseDto response = rankingService.voteForNationalRanking(entryId, voterId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("투표가 성공적으로 완료되었습니다.", response));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.badRequest("투표에 실패했습니다."));
            }
        } catch (Exception e) {
            log.error("전국 랭킹 투표 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("투표에 실패했습니다."));
        }
    }

    /**
     * 콘테스트 참가
     */
    @PostMapping("/participate")
    public ResponseEntity<ApiResponse<String>> participateInContest(
            @RequestParam Long mascotId,
            @RequestParam String contestType) {
        
        log.info("콘테스트 참가 요청 - mascotId: {}, contestType: {}", mascotId, contestType);
        
        try {
            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.parseLong(authentication.getName());
            
            // ContestType enum으로 변환
            com.solsolhey.solsol.entity.ContestEntry.ContestType type;
            try {
                type = com.solsolhey.solsol.entity.ContestEntry.ContestType.valueOf(contestType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.badRequest("잘못된 콘테스트 타입입니다. CAMPUS 또는 NATIONAL을 입력해주세요."));
            }
            
            rankingService.participateInContest(userId, mascotId, type);
            return ResponseEntity.ok(ApiResponse.success("참가 신청이 완료되었습니다.", "콘테스트 참가 성공"));
        } catch (Exception e) {
            log.error("콘테스트 참가 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("콘테스트 참가에 실패했습니다."));
        }
    }
}
