package com.solsolhey.common.controller;

import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.common.security.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Rate Limiting 테스트용 컨트롤러
 * 개발환경에서만 사용
 */
@RestController
@RequestMapping("/api/v1/test/rate-limit")
@RequiredArgsConstructor
@Slf4j
public class RateLimitTestController {

    private final RateLimitingService rateLimitingService;

    /**
     * Rate Limit 상태 확인
     */
    @GetMapping("/status/{ip}")
    public ResponseEntity<ApiResponse<RateLimitingService.RateLimitStatus>> getRateLimitStatus(
            @PathVariable String ip) {
        try {
            RateLimitingService.RateLimitStatus status = rateLimitingService.getIpStatus(ip);
            return ResponseEntity.ok(ApiResponse.success("Rate limit status retrieved", status));
        } catch (Exception e) {
            log.error("Error getting rate limit status for IP: {}", ip, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.internalServerError("Failed to get rate limit status"));
        }
    }

    /**
     * Rate Limit 테스트용 엔드포인트
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testRateLimit() {
        try {
            return ResponseEntity.ok(ApiResponse.success("Rate limit test endpoint", "Success"));
        } catch (Exception e) {
            log.error("Error in rate limit test endpoint", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.internalServerError("Test endpoint error"));
        }
    }
}
