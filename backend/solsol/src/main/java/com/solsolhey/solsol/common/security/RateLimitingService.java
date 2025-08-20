package com.solsolhey.solsol.common.security;

import lombok.extern.slf4j.Slf4j;
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

    // IP별 일반 API 요청 제한 캐시
    private final Map<String, TokenBucket> ipBucketCache = new ConcurrentHashMap<>();
    
    // IP별 로그인 시도 제한 캐시
    private final Map<String, TokenBucket> loginBucketCache = new ConcurrentHashMap<>();

    // 정리 작업을 위한 마지막 정리 시간
    private volatile LocalDateTime lastCleanupTime = LocalDateTime.now();

    /**
     * IP별 일반 API 요청 제한 확인
     * 분당 100회 제한
     * 
     * @param clientIp 클라이언트 IP
     * @return 요청 허용 여부
     */
    public boolean isAllowedByIp(String clientIp) {
        cleanupExpiredEntries();
        
        TokenBucket bucket = ipBucketCache.computeIfAbsent(clientIp, 
            k -> new TokenBucket(100, 100.0 / 60.0)); // 분당 100개 토큰
        
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
            k -> new TokenBucket(5, 5.0 / (15 * 60))); // 15분당 5개 토큰
        
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
        TokenBucket bucket = loginBucketCache.get(clientIp);
        if (bucket != null) {
            bucket.addTokens(1);
            log.debug("Login success bonus token granted for IP: {}", clientIp);
        }
    }

    /**
     * 특정 IP의 Rate Limit 상태 조회 (디버깅용)
     */
    public RateLimitStatus getIpStatus(String clientIp) {
        TokenBucket bucket = ipBucketCache.get(clientIp);
        if (bucket == null) {
            return new RateLimitStatus(true, -1, -1);
        }
        
        long availableTokens = (long) bucket.getAvailableTokens();
        long timeToRefill = availableTokens == 0 ? 60 : 0; // 간단한 추정
        
        return new RateLimitStatus(availableTokens > 0, availableTokens, timeToRefill);
    }

    /**
     * 만료된 엔트리 정리 (1시간마다)
     */
    private void cleanupExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        if (ChronoUnit.HOURS.between(lastCleanupTime, now) >= 1) {
            // 1시간 이상 사용되지 않은 엔트리 제거
            ipBucketCache.entrySet().removeIf(entry -> 
                ChronoUnit.HOURS.between(entry.getValue().getLastAccess(), now) >= 1);
            loginBucketCache.entrySet().removeIf(entry -> 
                ChronoUnit.HOURS.between(entry.getValue().getLastAccess(), now) >= 1);
            
            lastCleanupTime = now;
            log.debug("Rate limit cache cleanup completed");
        }
    }

    /**
     * Token Bucket 구현
     * 메모리 기반 Token Bucket 알고리즘
     */
    private static class TokenBucket {
        private final int capacity;              // 버킷 용량
        private final double tokensPerSecond;    // 초당 토큰 생성 비율
        private volatile double availableTokens; // 현재 사용 가능한 토큰 수
        private volatile LocalDateTime lastRefill; // 마지막 리필 시간
        private volatile LocalDateTime lastAccess; // 마지막 접근 시간

        public TokenBucket(int capacity, double tokensPerSecond) {
            this.capacity = capacity;
            this.tokensPerSecond = tokensPerSecond;
            this.availableTokens = capacity;
            this.lastRefill = LocalDateTime.now();
            this.lastAccess = LocalDateTime.now();
        }

        public synchronized boolean tryConsume(int tokens) {
            refill();
            this.lastAccess = LocalDateTime.now();
            
            if (availableTokens >= tokens) {
                availableTokens -= tokens;
                return true;
            }
            return false;
        }

        public synchronized void addTokens(int tokens) {
            refill();
            availableTokens = Math.min(capacity, availableTokens + tokens);
            lastAccess = LocalDateTime.now();
        }

        public synchronized double getAvailableTokens() {
            refill();
            return availableTokens;
        }

        public LocalDateTime getLastAccess() {
            return lastAccess;
        }

        private void refill() {
            LocalDateTime now = LocalDateTime.now();
            double secondsElapsed = ChronoUnit.MILLIS.between(lastRefill, now) / 1000.0;
            double tokensToAdd = secondsElapsed * tokensPerSecond;
            
            if (tokensToAdd > 0) {
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefill = now;
            }
        }
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
