package com.solsolhey.auth.controller;

import com.solsolhey.auth.dto.request.LoginRequestDto;
import com.solsolhey.auth.dto.request.SignUpRequestDto;
import com.solsolhey.auth.dto.response.LoginSuccessDto;
import com.solsolhey.auth.dto.response.SignUpResponse;
import com.solsolhey.auth.service.AuthService;
import com.solsolhey.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증/인가 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth API", description = "인증/인가 관련 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequestDto requestDto) {
        
        try {
            log.info("회원가입 API 호출: userId={}", requestDto.getUserId());
            SignUpResponse response = authService.signUp(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증을 수행하고 JWT 토큰을 반환합니다")
    public ResponseEntity<ApiResponse<LoginSuccessDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto) {
        
        try {
            log.info("로그인 API 호출: userId={}", requestDto.userId());
            ApiResponse<LoginSuccessDto> response = authService.login(requestDto);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 수행하고 토큰을 무효화합니다")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        try {
            log.info("로그아웃 API 호출");
            
            // Bearer 토큰에서 실제 토큰 추출
            String accessToken = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                accessToken = authorizationHeader.substring(7);
            }
            
            authService.logout(accessToken);
            return ResponseEntity.ok(
                ApiResponse.success("로그아웃이 완료되었습니다.", null)
            );
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
