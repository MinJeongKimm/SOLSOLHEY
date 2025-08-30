package com.solsolhey.common.controller;

import com.solsolhey.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 서버 상태 확인 컨트롤러
 * CSRF 토큰을 강제로 생성하여 SPA에서 사용할 수 있도록 함
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health(CsrfToken csrfToken) {
        // Declaring CsrfToken param materializes the token so CookieCsrfTokenRepository sets XSRF-TOKEN cookie.
        String token = csrfToken.getToken(); // ensure generation and read value

        Map<String, Object> healthInfo = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "SOLSOLHEY Backend",
            // Expose CSRF token in body so cross-origin SPAs can read it and send X-XSRF-TOKEN header
            "csrfToken", token
        );

        return ResponseEntity.ok(
            ApiResponse.success("서버가 정상적으로 작동 중입니다.", healthInfo)
        );
    }
}
