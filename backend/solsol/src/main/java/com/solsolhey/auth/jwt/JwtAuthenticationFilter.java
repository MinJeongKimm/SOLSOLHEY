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
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // 강제 확인용 출력 (로그 레벨 무관)
        System.out.println("=== JWT FILTER START ===");
        System.out.println("JWT FILTER >>> " + request.getMethod() + " " + request.getRequestURI());
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(c.getName())) {
                    System.out.println("ACCESS_TOKEN(cookie) PRESENT, length=" + c.getValue().length());
                }
            }
        }
        
        try {
            log.debug("=== JWT filter start: {} {}", request.getMethod(), request.getRequestURI());

            String token = extractTokenFromRequest(request);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateAccessToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUserId(userId);

                var authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                log.debug("JWT authenticated: principal={}, uri={}", userDetails.getUsername(), request.getRequestURI());
            } else {
                log.debug("JWT not applied (missing/invalid): {}", request.getRequestURI());
            }

            filterChain.doFilter(request, response);

        } catch (com.solsolhey.common.exception.EntityNotFoundException nf) {
            // User not found / inactive → clear and continue as anonymous
            log.warn("JWT user load failed; proceeding anonymous: {}", nf.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);

        } catch (AuthException.TokenExpiredException te) {
            // Expired → clear and continue as anonymous; client should refresh
            log.info("JWT expired; proceeding anonymous: {}", te.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Any other unexpected issue → clear and continue so we see a proper 401/403 later
            log.error("JWT filter unexpected error; proceeding anonymous: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 요청에서 JWT 토큰 추출
     * - 운영: 쿠키만
     * - 개발: 쿠키 우선 + Authorization 헤더 fallback 허용
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        log.debug("=== 토큰 추출 시작 ===");
        
        // 쿠키에서 ACCESS_TOKEN 확인
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.debug("쿠키 개수: {}", cookies.length);
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                log.debug("쿠키 검사: {} = {}", cookie.getName(), cookie.getValue());
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    log.debug("ACCESS_TOKEN 쿠키 발견: {}", cookie.getValue());
                    return java.net.URLDecoder.decode(cookie.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        } else {
            log.debug("쿠키가 없음");
        }
        
        // 개발환경에서만 Authorization 헤더 fallback 허용
        if (isDevelopmentEnvironment()) {
            String bearer = request.getHeader("Authorization");
            log.debug("Authorization 헤더: {}", bearer);
            if (org.springframework.util.StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);
                log.debug("Bearer 토큰 추출: {}", token);
                return token;
            }
        }
        
        log.debug("토큰을 찾을 수 없음");
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
        if (path.equals("/health") ||
            path.equals("/api/v1/auth/login") ||
            path.equals("/api/v1/auth/signup") ||
            path.equals("/api/v1/auth/refresh") ||
            path.equals("/api/v1/auth/logout") ||
            path.startsWith("/public/")) {
            return true;
        }
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
