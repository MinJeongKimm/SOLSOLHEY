package com.solsolhey.point.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.point.dto.request.PointEarnRequest;
import com.solsolhey.point.dto.request.PointSpendRequest;
import com.solsolhey.point.dto.response.PointStatsResponse;
import com.solsolhey.point.dto.response.PointTransactionResponse;
import com.solsolhey.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 포인트 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
@Slf4j
public class PointController {

    private final PointService pointService;

    /**
     * 포인트 적립
     */
    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> earnPoints(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PointEarnRequest request) {
        try {
            log.info("포인트 적립 API 호출: userId={}", userDetails.getUserId());
            PointTransactionResponse response = pointService.earnPoints(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("포인트를 적립했습니다.", response));
        } catch (Exception e) {
            log.error("포인트 적립 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 포인트 사용
     */
    @PostMapping("/spend")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> spendPoints(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PointSpendRequest request) {
        try {
            log.info("포인트 사용 API 호출: userId={}", userDetails.getUserId());
            PointTransactionResponse response = pointService.spendPoints(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("포인트를 사용했습니다.", response));
        } catch (Exception e) {
            log.error("포인트 사용 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 일일 보너스 포인트 지급
     */
    @PostMapping("/daily-bonus")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> giveDailyBonus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("일일 보너스 API 호출: userId={}", userDetails.getUserId());
            PointTransactionResponse response = pointService.giveDailyBonus(userDetails.getUser());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("일일 보너스를 받았습니다.", response));
        } catch (Exception e) {
            log.error("일일 보너스 지급 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 포인트 거래 내역 조회
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<PointTransactionResponse>>> getUserTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("포인트 거래 내역 API 호출: userId={}", userDetails.getUserId());
            Page<PointTransactionResponse> transactions = pointService.getUserTransactions(userDetails.getUser(), pageable);
            return ResponseEntity.ok(ApiResponse.success("거래 내역을 조회했습니다.", transactions));
        } catch (Exception e) {
            log.error("거래 내역 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 특정 타입 거래 내역 조회
     */
    @GetMapping("/transactions/{transactionType}")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getTransactionsByType(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String transactionType) {
        try {
            log.info("타입별 거래 내역 API 호출: userId={}, type={}", userDetails.getUserId(), transactionType);
            List<PointTransactionResponse> transactions = pointService.getTransactionsByType(userDetails.getUser(), transactionType);
            return ResponseEntity.ok(ApiResponse.success("타입별 거래 내역을 조회했습니다.", transactions));
        } catch (Exception e) {
            log.error("타입별 거래 내역 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 기간별 거래 내역 조회
     */
    @GetMapping("/transactions/date-range")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getTransactionsByDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("기간별 거래 내역 API 호출: userId={}, startDate={}, endDate={}", 
                    userDetails.getUserId(), startDate, endDate);
            List<PointTransactionResponse> transactions = pointService.getTransactionsByDateRange(
                    userDetails.getUser(), startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("기간별 거래 내역을 조회했습니다.", transactions));
        } catch (Exception e) {
            log.error("기간별 거래 내역 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 포인트 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PointStatsResponse>> getUserPointStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("포인트 통계 API 호출: userId={}", userDetails.getUserId());
            PointStatsResponse stats = pointService.getUserPointStats(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("포인트 통계를 조회했습니다.", stats));
        } catch (Exception e) {
            log.error("포인트 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 월별 포인트 통계 조회
     */
    @GetMapping("/stats/monthly/{year}")
    public ResponseEntity<ApiResponse<Map<Integer, PointStatsResponse>>> getMonthlyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int year) {
        try {
            log.info("월별 포인트 통계 API 호출: userId={}, year={}", userDetails.getUserId(), year);
            Map<Integer, PointStatsResponse> monthlyStats = pointService.getMonthlyStats(userDetails.getUser(), year);
            return ResponseEntity.ok(ApiResponse.success("월별 포인트 통계를 조회했습니다.", monthlyStats));
        } catch (Exception e) {
            log.error("월별 포인트 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 현재 포인트 잔액 조회
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Integer>> getCurrentBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("포인트 잔액 조회 API 호출: userId={}", userDetails.getUserId());
            Integer balance = pointService.getCurrentBalance(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("포인트 잔액을 조회했습니다.", balance));
        } catch (Exception e) {
            log.error("포인트 잔액 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 오늘 획득한 포인트 조회
     */
    @GetMapping("/today-earned")
    public ResponseEntity<ApiResponse<Integer>> getTodayEarnedPoints(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("오늘 획득 포인트 조회 API 호출: userId={}", userDetails.getUserId());
            Integer todayEarned = pointService.getTodayEarnedPoints(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("오늘 획득한 포인트를 조회했습니다.", todayEarned));
        } catch (Exception e) {
            log.error("오늘 획득 포인트 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 포인트 환불 (관리자용)
     */
    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> refundPoints(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long transactionId,
            @RequestParam String reason) {
        try {
            log.info("포인트 환불 API 호출: userId={}, transactionId={}", userDetails.getUserId(), transactionId);
            PointTransactionResponse response = pointService.refundPoints(userDetails.getUser(), transactionId, reason);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("포인트를 환불했습니다.", response));
        } catch (Exception e) {
            log.error("포인트 환불 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 관리자 포인트 지급
     */
    @PostMapping("/admin-give")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> adminGivePoints(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Integer amount,
            @RequestParam String reason) {
        try {
            log.info("관리자 포인트 지급 API 호출: userId={}, amount={}", userDetails.getUserId(), amount);
            PointTransactionResponse response = pointService.adminGivePoints(userDetails.getUser(), amount, reason);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("포인트를 지급했습니다.", response));
        } catch (Exception e) {
            log.error("관리자 포인트 지급 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
