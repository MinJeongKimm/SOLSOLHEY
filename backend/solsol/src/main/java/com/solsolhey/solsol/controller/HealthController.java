package com.solsolhey.solsol.controller;

import com.solsolhey.solsol.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 애플리케이션 상태 확인 컨트롤러
 */
@RestController
public class HealthController {

    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    /**
     * 인증이 필요한 테스트 엔드포인트
     */
    @GetMapping("/test/auth")
    public ApiResponse<String> authTest() {
        return ApiResponse.success("인증된 사용자만 접근 가능한 API입니다.", "Auth Test Success");
    }

    /**
     * 공개 테스트 엔드포인트
     */
    @GetMapping("/public/test")
    public ApiResponse<String> publicTest() {
        return ApiResponse.success("누구나 접근 가능한 공개 API입니다.", "Public Test Success");
    }
}
