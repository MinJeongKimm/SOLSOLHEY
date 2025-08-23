package com.solsolhey.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting 서비스
 * 메모리 기반 Token Bucket 알고리즘 구현
 */
@Service
@Slf4j
public class RateLimitingService {

    @Value("${rate-limit.general.requests-per-minute:10}")
    private int generalRequestsPerMinute;
    
    @Value("${rate-limit.login.attempts-per-15min:5}")
    private int loginAttemptsPer15Min;
    
    @Value("${rate-limit.cleanup.interval-hours:1}")
    private int cleanupIntervalHours;

    // IP별 일반 API 요청 제한 캐시
    private final Map<String, TokenBucket> ipBucketCache = new ConcurrentHashMap<>();
    
    // IP별 로그인 시도 제한 캐시
    private final Map<String, TokenBucket> loginBucketCache = new ConcurrentHashMap<>();

    // 정리 작업을 위한 마지막 정리 시간
    private volatile LocalDateTime lastCleanupTime = LocalDateTime.now();

    /**
     * IP별 일반 API 요청 제한 확인
     * 분당 설정값만큼 제한
     * 
     * @param clientIp 클라이언트 IP
     * @return 요청 허용 여부
     */
    public boolean isAllowedByIp(String clientIp) {
        cleanupExpiredEntries();
        
        int limit = generalRequestsPerMinute;        // e.g., 10
        int cap   = Math.max(1, (int) Math.ceil(limit * 0.6)); // 60% of limit
        double perSec = limit / 60.0;
        
        TokenBucket bucket = ipBucketCache.computeIfAbsent(clientIp,
            k -> new TokenBucket(cap, perSec));
        
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
        }
        
        return allowed;
    }

    /**
     * 로그인 시도 제한 확인  
     * IP별 15분당 5회 로그인 시도 제한
     * 
     * @param clientIp 클라이언트 IP
     * @return 로그인 시도 허용 여부
     */
    public boolean isLoginAllowed(String clientIp) {
        cleanupExpiredEntries();
        
        TokenBucket bucket = loginBucketCache.computeIfAbsent(clientIp, 
            k -> new TokenBucket(loginAttemptsPer15Min, (double) loginAttemptsPer15Min / (15 * 60))); // 15분당 설정값만큼 토큰
        
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("Login rate limit exceeded for IP: {}", clientIp);
        }
        
        return allowed;
    }

    /**
     * 로그인 성공 시 보상 토큰 지급 (선택적)
     * 성공한 로그인에 대해서는 토큰을 일부 복원
     */
    public void onLoginSuccess(String clientIp) {
        // 새로운 TokenBucket 구현에서는 addTokens 메서드가 없으므로 제거
        log.debug("Login success for IP: {}", clientIp);
    }

    /**
     * 특정 IP의 Rate Limit 상태 조회 (디버깅용)
     */
    public RateLimitStatus getIpStatus(String clientIp) {
        TokenBucket bucket = ipBucketCache.get(clientIp);
        if (bucket == null) {
            return new RateLimitStatus(true, -1, -1);
        }
        
        int availableTokens = bucket.remaining();
        long timeToRefill = availableTokens == 0 ? 60 : 0; // 간단한 추정
        
        return new RateLimitStatus(availableTokens > 0, availableTokens, timeToRefill);
    }

    /**
     * 특정 IP의 일반 API Rate Limit 남은 토큰 수 조회
     */
    public int remainingGeneral(String clientIp) {
        TokenBucket bucket = ipBucketCache.get(clientIp);
        if (bucket == null) {
            return generalRequestsPerMinute;
        }
        return bucket.remaining();
    }

    /**
     * 일반 API 분당 요청 제한 수 조회
     */
    public int getGeneralRequestsPerMinute() {
        return generalRequestsPerMinute;
    }

    /**
     * 만료된 엔트리 정리 (1시간마다)
     */
    private void cleanupExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        if (ChronoUnit.HOURS.between(lastCleanupTime, now) >= cleanupIntervalHours) {
            // 설정된 시간 이상 사용되지 않은 엔트리 제거
            ipBucketCache.entrySet().removeIf(entry -> 
                ChronoUnit.HOURS.between(entry.getValue().getLastAccess(), now) >= cleanupIntervalHours);
            loginBucketCache.entrySet().removeIf(entry -> 
                ChronoUnit.HOURS.between(entry.getValue().getLastAccess(), now) >= cleanupIntervalHours);
            
            lastCleanupTime = now;
            log.debug("Rate limit cache cleanup completed");
        }
    }

    /**
     * Token Bucket 구현
     * 메모리 기반 Token Bucket 알고리즘
     */
    private static class TokenBucket {
        private final int capacity;
        private final double tokensPerSec;
        private double tokens;
        private long lastRefillTime = System.currentTimeMillis();
        private volatile java.time.LocalDateTime lastAccess = java.time.LocalDateTime.now();

        TokenBucket(int capacity, double tokensPerSec) {
            this.capacity = capacity;
            this.tokensPerSec = tokensPerSec;
            this.tokens = capacity; // start with a limited burst
        }

        synchronized boolean tryConsume(int permits) {
            long now = System.currentTimeMillis();
            double deltaSec = (now - lastRefillTime) / 1000.0;
            lastRefillTime = now;

            // refill - 더 정확한 계산
            if (deltaSec > 0) {
                double refillAmount = deltaSec * tokensPerSec;
                tokens = Math.min(capacity, tokens + refillAmount);
            }

            if (tokens >= permits) {
                tokens -= permits;
                lastAccess = java.time.LocalDateTime.now();
                return true;
            }
            return false;
        }

        synchronized int remaining() {
            return Math.max(0, (int) Math.floor(tokens));
        }

        java.time.LocalDateTime getLastAccess() { return lastAccess; }
    }

    /**
     * Rate Limit 상태 정보
     */
    public record RateLimitStatus(
            boolean allowed,
            long availableTokens,
            long secondsToRefill
    ) {}
}
