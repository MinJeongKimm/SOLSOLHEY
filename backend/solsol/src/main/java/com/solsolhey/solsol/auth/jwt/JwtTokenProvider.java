package com.solsolhey.solsol.auth.jwt;

import com.solsolhey.solsol.common.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenExpiration;
        this.refreshTokenValidityInMilliseconds = refreshTokenExpiration;
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
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
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
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
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
}
