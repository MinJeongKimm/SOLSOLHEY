package com.solsolhey.solsol.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JWT 토큰 블랙리스트 관리 서비스
 * 로그아웃된 토큰을 무효화하여 보안을 강화
 */
@Service
@Slf4j
public class TokenBlacklistService {

    // 메모리 기반 블랙리스트 (토큰 -> 만료시간)
    private final ConcurrentHashMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    
    // 통계 정보
    private final AtomicLong totalBlacklisted = new AtomicLong(0);
    private final AtomicLong totalCleaned = new AtomicLong(0);

    @PostConstruct
    public void init() {
        log.info("TokenBlacklistService 초기화 완료");
    }

    /**
     * 토큰을 블랙리스트에 추가
     * 
     * @param token 블랙리스트에 추가할 JWT 토큰
     * @param expirationTime 토큰 만료 시간
     */
    public void blacklistToken(String token, LocalDateTime expirationTime) {
        if (token == null || token.isBlank()) {
            log.warn("빈 토큰을 블랙리스트에 추가하려고 시도");
            return;
        }

        // 토큰 해시를 키로 사용 (메모리 효율성)
        String tokenKey = generateTokenKey(token);
        
        blacklistedTokens.put(tokenKey, expirationTime);
        totalBlacklisted.incrementAndGet();
        
        log.debug("토큰 블랙리스트 추가: {} (만료: {})", 
                tokenKey.substring(0, Math.min(10, tokenKey.length())) + "...", 
                expirationTime);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * @param token 확인할 JWT 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String tokenKey = generateTokenKey(token);
        boolean isBlacklisted = blacklistedTokens.containsKey(tokenKey);
        
        if (isBlacklisted) {
            log.debug("블랙리스트된 토큰 사용 시도 감지: {}", 
                    tokenKey.substring(0, Math.min(10, tokenKey.length())) + "...");
        }
        
        return isBlacklisted;
    }

    /**
     * 토큰 키 생성 (해시 기반)
     * 메모리 효율성을 위해 토큰의 해시코드를 사용
     */
    private String generateTokenKey(String token) {
        // JWT는 보통 길기 때문에 해시코드 사용
        // 실제 프로덕션에서는 SHA-256 등을 고려할 수 있음
        return String.valueOf(token.hashCode());
    }

    /**
     * 만료된 토큰들을 블랙리스트에서 자동 정리
     * 매 시간마다 실행
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다 (1000ms * 60 * 60)
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int beforeSize = blacklistedTokens.size();
        
        // 만료된 토큰들 제거
        blacklistedTokens.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isBefore(now);
            if (expired) {
                totalCleaned.incrementAndGet();
            }
            return expired;
        });
        
        int afterSize = blacklistedTokens.size();
        int cleaned = beforeSize - afterSize;
        
        if (cleaned > 0) {
            log.info("만료된 토큰 정리 완료: {} 개 제거, 현재 블랙리스트 크기: {}", cleaned, afterSize);
        }
    }

    /**
     * 블랙리스트 상태 정보 조회
     */
    public BlacklistStatus getStatus() {
        return new BlacklistStatus(
                blacklistedTokens.size(),
                totalBlacklisted.get(),
                totalCleaned.get()
        );
    }

    /**
     * 전체 블랙리스트 초기화 (테스트용)
     */
    public void clearAll() {
        int size = blacklistedTokens.size();
        blacklistedTokens.clear();
        log.warn("전체 블랙리스트 초기화: {} 개 토큰 삭제", size);
    }

    /**
     * 블랙리스트 상태 정보를 담는 record
     */
    public record BlacklistStatus(
            int currentSize,      // 현재 블랙리스트 크기
            long totalBlacklisted, // 총 블랙리스트 추가된 토큰 수
            long totalCleaned     // 총 정리된 토큰 수
    ) {}
}
