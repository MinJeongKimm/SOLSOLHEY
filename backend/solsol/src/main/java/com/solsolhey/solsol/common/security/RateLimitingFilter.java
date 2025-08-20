package com.solsolhey.solsol.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Rate Limiting 필터
 * 모든 HTTP 요청에 대해 Rate Limiting 적용
 */
// @Component  // 임시 비활성화 - 토큰 블랙리스트 테스트 완료 후 재활성화 예정
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter implements Filter {

    private final RateLimitingService rateLimitingService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIpAddress(httpRequest);
        String requestPath = httpRequest.getRequestURI();
        
        // Rate Limiting 적용 여부 확인
        if (!shouldApplyRateLimit(requestPath)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 로그인 엔드포인트에 대한 특별한 제한
        if (isLoginEndpoint(requestPath)) {
            if (!rateLimitingService.isLoginAllowed(clientIp)) {
                handleRateLimitExceeded(httpResponse, "로그인 시도가 너무 많습니다. 15분 후 다시 시도해주세요.", 
                        HttpStatus.TOO_MANY_REQUESTS);
                return;
            }
        }
        
        // 일반 API에 대한 Rate Limiting
        if (!rateLimitingService.isAllowedByIp(clientIp)) {
            handleRateLimitExceeded(httpResponse, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", 
                    HttpStatus.TOO_MANY_REQUESTS);
            return;
        }
        
        // Rate Limiting 통과, 다음 필터로 진행
        chain.doFilter(request, response);
    }

    /**
     * 클라이언트 IP 주소 추출
     * 프록시나 로드밸런서를 고려한 실제 IP 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            // 첫 번째 IP가 실제 클라이언트 IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        
        String xForwarded = request.getHeader("X-Forwarded");
        if (StringUtils.hasText(xForwarded)) {
            return xForwarded;
        }
        
        String forwarded = request.getHeader("Forwarded");
        if (StringUtils.hasText(forwarded)) {
            return forwarded;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Rate Limiting을 적용할지 여부 결정
     */
    private boolean shouldApplyRateLimit(String requestPath) {
        // 정적 리소스는 제외
        if (requestPath.startsWith("/static/") || 
            requestPath.startsWith("/css/") || 
            requestPath.startsWith("/js/") || 
            requestPath.startsWith("/images/") ||
            requestPath.endsWith(".ico")) {
            return false;
        }
        
        // 헬스체크는 제외 (모니터링 도구 등)
        if (requestPath.equals("/health") || requestPath.equals("/actuator/health")) {
            return false;
        }
        
        // H2 Console은 개발환경에서만 동작하므로 제외하고, 나머지는 적용
        return !requestPath.startsWith("/h2-console");
    }

    /**
     * 로그인 엔드포인트 여부 확인
     */
    private boolean isLoginEndpoint(String requestPath) {
        return requestPath.equals("/api/v1/auth/login") || requestPath.equals("/auth/login");
    }

    /**
     * Rate Limit 초과 시 응답 처리
     */
    private void handleRateLimitExceeded(HttpServletResponse response, String message, HttpStatus status) 
            throws IOException {
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Rate Limit 관련 헤더 추가
        response.setHeader("X-RateLimit-Limit", "100");  // 분당 제한
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000)); // 1분 후
        response.setHeader("Retry-After", "60"); // 60초 후 재시도
        
        // JSON 응답을 직접 생성 (ObjectMapper 순환 참조 방지)
        String jsonResponse = String.format(
            "{\"success\":false,\"code\":%d,\"message\":\"%s\",\"timestamp\":\"%s\"}", 
            status.value(), 
            message.replace("\"", "\\\""),  // 따옴표 이스케이프
            java.time.LocalDateTime.now().toString()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
