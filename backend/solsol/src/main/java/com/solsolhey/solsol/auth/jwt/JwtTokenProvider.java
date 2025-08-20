package com.solsolhey.solsol.auth.jwt;

import com.solsolhey.solsol.auth.service.TokenBlacklistService;
import com.solsolhey.solsol.common.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            TokenBlacklistService tokenBlacklistService) {
        
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenExpiration;
        this.refreshTokenValidityInMilliseconds = refreshTokenExpiration;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검증 (블랙리스트 확인 포함)
     */
    public boolean validateToken(String token) {
        try {
            // 1. 블랙리스트 확인 (가장 먼저 체크)
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("블랙리스트된 토큰 사용 시도");
                return false;
            }

            // 2. JWT 서명 및 구조 검증
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new AuthException.TokenExpiredException();
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        Object userId = claims.get("userId");
        
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else {
            throw new AuthException.InvalidTokenException("Invalid userId in token");
        }
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("username", String.class);
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     */
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("tokenType", String.class);
    }

    /**
     * 토큰에서 Claims 파싱
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new AuthException.InvalidTokenException("Failed to parse token claims");
        }
    }

    /**
     * 토큰 만료까지 남은 시간 (밀리초)
     */
    public long getRemainingTimeFromToken(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Access Token 검증 (토큰 타입까지 확인)
     */
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        
        String tokenType = getTokenType(token);
        return "access".equals(tokenType);
    }

    /**
     * Refresh Token 검증 (토큰 타입까지 확인)
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        
        String tokenType = getTokenType(token);
        return "refresh".equals(tokenType);
    }

    /**
     * 토큰을 블랙리스트에 추가
     * 로그아웃 시 호출됨
     */
    public void blacklistToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expirationDate = claims.getExpiration();
            
            // Date를 LocalDateTime으로 변환
            LocalDateTime expirationTime = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            tokenBlacklistService.blacklistToken(token, expirationTime);
            log.info("토큰이 블랙리스트에 추가됨");
        } catch (Exception e) {
            log.error("토큰 블랙리스트 추가 실패: {}", e.getMessage());
        }
    }

    /**
     * 토큰의 만료 시간을 LocalDateTime으로 반환
     */
    public LocalDateTime getExpirationAsLocalDateTime(String token) {
        Claims claims = parseClaims(token);
        Date expirationDate = claims.getExpiration();
        return expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 토큰 블랙리스트 상태 조회
     */
    public TokenBlacklistService.BlacklistStatus getBlacklistStatus() {
        return tokenBlacklistService.getStatus();
    }
}
