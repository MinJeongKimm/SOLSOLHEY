package com.solsolhey.common.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate Limiting 필터
 * 모든 HTTP 요청에 대해 Rate Limiting 적용
 */
//@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter implements Filter {

    @Value("${RATE_LIMIT_ENABLED:true}")
    private boolean rateLimitEnabled;

    private final RateLimitingService rateLimitingService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIpAddress(httpRequest);
        String requestPath = httpRequest.getRequestURI();
        
        // Rate Limiting 활성화 여부 확인
        if (!rateLimitEnabled) {
            chain.doFilter(request, response);
            return;
        }
        
        // Rate Limiting 적용 여부 확인
        if (!shouldApplyRateLimit(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Rate Limit 헤더 설정
        httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitingService.getGeneralRequestsPerMinute()));
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(rateLimitingService.remainingGeneral(clientIp), 0)));
        // Dev-only hint:
        httpResponse.setHeader("Retry-After", "60");
        
        boolean isAllowed = true;
        String errorMessage = "";
        
        // 로그인 엔드포인트에 대한 특별한 제한
        if (isLoginEndpoint(requestPath)) {
            if (!rateLimitingService.isLoginAllowed(clientIp)) {
                isAllowed = false;
                errorMessage = "로그인 시도가 너무 많습니다. 15분 후 다시 시도해주세요.";
            }
        } else {
            // 일반 API에 대한 Rate Limiting
            if (!rateLimitingService.isAllowedByIp(clientIp)) {
                isAllowed = false;
                errorMessage = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
            }
        }
        
        if (!isAllowed) {
            // 429 응답으로 차단
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"message\":\"Too Many Requests\"}");
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
    private boolean shouldApplyRateLimit(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        // OPTIONS 요청 제외 (preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) return false;
        
        // 헬스체크 및 액추에이터 제외
        if (path.startsWith("/health") || path.startsWith("/actuator")) return false;
        
        // 정적 리소스 제외
        if (path.startsWith("/static/") || 
            path.endsWith(".js") || 
            path.endsWith(".css") ||
            path.endsWith(".png") || 
            path.endsWith(".jpg") || 
            path.endsWith(".ico")) {
            return false;
        }
        
        // H2 Console 제외
        if (path.startsWith("/h2-console")) return false;
        
        return true;
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
        response.setHeader("X-RateLimit-Limit", "100");  // 분당 제한 (설정값으로 변경 예정)
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
