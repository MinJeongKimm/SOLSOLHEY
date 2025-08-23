package com.solsolhey.common.controller;

import com.solsolhey.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 서버 상태 확인 컨트롤러
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "SOLSOLHEY Backend"
        );
        
        return ResponseEntity.ok(
            ApiResponse.success("서버가 정상적으로 작동 중입니다.", healthInfo)
        );
    }
}
