package com.solsolhey.finance.controller;

import com.solsolhey.finance.dto.ExchangeEstimateRequest;
import com.solsolhey.finance.dto.ExchangeEstimateResponse;
import com.solsolhey.finance.dto.ExchangeRateResponse;
import com.solsolhey.finance.dto.TransactionHistoryRequest;
import com.solsolhey.finance.dto.TransactionHistoryResponse;
import com.solsolhey.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Finance API", description = "금융 서비스 API")
public class FinanceController {
    
    private final FinanceService financeService;
    
    @Operation(summary = "환율 전체 조회", description = "모든 통화의 환율 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/exchange-rates")
    public Mono<ResponseEntity<Map<String, Object>>> getAllExchangeRates(
            @AuthenticationPrincipal Authentication authentication,
            @Parameter(description = "기준 통화 코드 (선택사항)", example = "USD")
            @RequestParam(required = false) String base) {
        
        Long userId = getUserId(authentication);
        log.info("환율 전체 조회 API 호출 - 사용자 ID: {}, base: {}", userId, base);
        
        return financeService.getAllExchangeRates(base)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("result", "success".equals(response.getCode()) ? "SUCCESS" : "ERROR");
                    result.put("message", response.getMessage() != null ? response.getMessage() : 
                            ("success".equals(response.getCode()) ? "환율 조회가 완료되었습니다." : "환율 조회에 실패했습니다."));
                    result.put("data", response.getPayload());
                    
                    HttpStatus status = "success".equals(response.getCode()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
                    return ResponseEntity.status(status).body(result);
                })
                .onErrorResume(error -> {
                    log.error("환율 전체 조회 중 오류 발생", error);
                    return Mono.just(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "환율 조회에 실패했습니다."));
                });
    }
    
    @Operation(summary = "환율 단건 조회", description = "특정 통화의 환율 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/exchange-rate")
    public Mono<ResponseEntity<Map<String, Object>>> getExchangeRate(
            @AuthenticationPrincipal Authentication authentication,
            @Parameter(description = "기준 통화 코드", example = "USD", required = true)
            @RequestParam String base) {
        
        Long userId = getUserId(authentication);
        log.info("환율 단건 조회 API 호출 - 사용자 ID: {}, base: {}", userId, base);
        
        return financeService.getExchangeRate(base)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("result", "success".equals(response.getCode()) ? "SUCCESS" : "ERROR");
                    result.put("message", response.getMessage() != null ? response.getMessage() : 
                            ("success".equals(response.getCode()) ? "환율 조회가 완료되었습니다." : "환율 조회에 실패했습니다."));
                    result.put("data", response.getPayload());
                    
                    HttpStatus status = "success".equals(response.getCode()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
                    return ResponseEntity.status(status).body(result);
                })
                .onErrorResume(error -> {
                    log.error("환율 단건 조회 중 오류 발생", error);
                    HttpStatus status = error instanceof IllegalArgumentException ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(createErrorResponse(status, error.getMessage()));
                });
    }
    
    @Operation(summary = "환전 예상 금액 조회", description = "환전 시 예상되는 금액을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/exchange/estimate")
    public Mono<ResponseEntity<Map<String, Object>>> getExchangeEstimate(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody ExchangeEstimateRequest request) {
        
        Long userId = getUserId(authentication);
        log.info("환전 예상 금액 조회 API 호출 - 사용자 ID: {}", userId);
        
        return financeService.getExchangeEstimate(request)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("result", "success".equals(response.getCode()) ? "SUCCESS" : "ERROR");
                    result.put("message", response.getMessage() != null ? response.getMessage() : 
                            ("success".equals(response.getCode()) ? "환전 예상 금액 조회가 완료되었습니다." : "환전 예상 금액 조회에 실패했습니다."));
                    result.put("data", response.getPayload());
                    
                    HttpStatus status = "success".equals(response.getCode()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
                    return ResponseEntity.status(status).body(result);
                })
                .onErrorResume(error -> {
                    log.error("환전 예상 금액 조회 중 오류 발생", error);
                    return Mono.just(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "환전 예상 금액 조회에 실패했습니다."));
                });
    }
    
    @Operation(summary = "계좌 거래내역 조회", description = "계좌의 거래내역을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/accounts/transactions")
    public Mono<ResponseEntity<Map<String, Object>>> getTransactionHistory(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody TransactionHistoryRequest request) {
        
        Long userId = getUserId(authentication);
        log.info("계좌 거래내역 조회 API 호출 - 사용자 ID: {}", userId);
        
        return financeService.getTransactionHistory(request)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("result", "success".equals(response.getCode()) ? "SUCCESS" : "ERROR");
                    result.put("message", response.getMessage() != null ? response.getMessage() : 
                            ("success".equals(response.getCode()) ? "거래내역 조회가 완료되었습니다." : "거래내역 조회에 실패했습니다."));
                    result.put("data", response.getPayload());
                    
                    HttpStatus status = "success".equals(response.getCode()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
                    return ResponseEntity.status(status).body(result);
                })
                .onErrorResume(error -> {
                    log.error("계좌 거래내역 조회 중 오류 발생", error);
                    return Mono.just(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "거래내역 조회에 실패했습니다."));
                });
    }
    
    /**
     * Authentication에서 사용자 ID 추출
     * TODO: 실제 인증 구현에 맞게 수정 필요
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return 1L; // 기본 사용자 ID
        }
        
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            log.warn("사용자 ID 파싱 실패: {}", authentication.getName());
            return 1L; // 기본 사용자 ID
        }
    }
    
    /**
     * 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("result", "ERROR");
        errorResponse.put("message", message);
        errorResponse.put("data", null);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}
