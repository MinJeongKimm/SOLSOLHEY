package com.solsolhey.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solsolhey.common.exception.AuthException;
import com.solsolhey.auth.service.CustomUserDetailsService;
import com.solsolhey.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (StringUtils.hasText(token)) {
                // Access Token 검증
                if (!jwtTokenProvider.validateAccessToken(token)) {
                    throw new AuthException.InvalidTokenException();
                }
                
                // 토큰에서 사용자 정보 추출
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String username = jwtTokenProvider.getUsernameFromToken(token);
                
                // UserDetails 로드
                UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
                
                // Spring Security Context에 인증 정보 설정
                UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                
                log.debug("Successfully authenticated user: {} (ID: {})", username, userId);
            }
            
            filterChain.doFilter(request, response);
            
        } catch (AuthException e) {
            handleAuthenticationException(response, e);
        } catch (RuntimeException e) {
            log.error("JWT authentication error: ", e);
            handleAuthenticationException(response, 
                new AuthException.InvalidTokenException("토큰 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * 인증 예외 처리
     */
    private void handleAuthenticationException(HttpServletResponse response, AuthException e) 
            throws IOException {
        
        // 기본적으로 401 Unauthorized 응답
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Object> errorResponse = ApiResponse.error(status, e.getMessage());
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 필터를 적용하지 않을 경로들
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 공통 인증 불필요 경로들
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/api/public/") || 
            path.equals("/api/health")) {
            return true;
        }
        
        // 개발환경에서만 인증 불필요 경로들
        if (isDevelopmentEnvironment()) {
            return path.startsWith("/h2-console") ||
                   path.startsWith("/swagger-ui") ||
                   path.startsWith("/v3/api-docs");
        }
        
        return false;
    }

    /**
     * 개발환경 여부 확인
     * local, dev 프로파일인 경우 개발환경으로 판단
     */
    private boolean isDevelopmentEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        
        // 활성 프로파일이 없을 때는 기본값 확인
        if (activeProfiles.length == 0) {
            String[] defaultProfiles = environment.getDefaultProfiles();
            activeProfiles = defaultProfiles.length > 0 ? defaultProfiles : new String[]{"default"};
        }
        
        for (String profile : activeProfiles) {
            if ("local".equals(profile) || "dev".equals(profile) || "default".equals(profile)) {
                return true;
            }
        }
        
        return false;
    }
}
