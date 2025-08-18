package com.solsolhey.solsol.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.solsol.auth.dto.LoginRequestDto;
import com.solsolhey.solsol.auth.dto.LoginResponseDto;
import com.solsolhey.solsol.auth.dto.RefreshTokenRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpResponseDto;
import com.solsolhey.solsol.auth.dto.TokenResponseDto;
import com.solsolhey.solsol.auth.service.AuthService;
import com.solsolhey.solsol.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증/인가 관련 컨트롤러
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signUp(
            @Valid @RequestBody SignUpRequestDto requestDto) {
        
        log.info("회원가입 요청: username={}", requestDto.getUsername());
        
        SignUpResponseDto response = authService.signUp(requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto) {
        
        log.info("로그인 요청: usernameOrEmail={}", requestDto.getUsernameOrEmail());
        
        LoginResponseDto response = authService.login(requestDto);
        
        return ResponseEntity.ok(
                ApiResponse.success("로그인이 완료되었습니다.", response));
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refreshToken(
            @RequestBody RefreshTokenRequestDto requestDto) {
        
        log.info("토큰 갱신 요청");
        
        TokenResponseDto response = authService.refreshToken(requestDto.getRefreshToken());
        
        return ResponseEntity.ok(
                ApiResponse.success("토큰이 갱신되었습니다.", response));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        
        log.info("로그아웃 요청");
        
        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            authService.logout(accessToken);
        }
        
        return ResponseEntity.ok(
                ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }

    /**
     * 회원가입 - 사용자명 중복 체크
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(
            @RequestParam String username) {
        
        log.info("사용자명 중복 체크: {}", username);
        
        boolean isAvailable = !authService.isUsernameExists(username);
        String message = isAvailable ? "사용 가능한 사용자명입니다." : "이미 사용 중인 사용자명입니다.";
        
        return ResponseEntity.ok(
                ApiResponse.success(message, isAvailable));
    }

    /**
     * 회원가입 - 이메일 중복 체크
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(
            @RequestParam String email) {
        
        log.info("이메일 중복 체크: {}", email);
        
        boolean isAvailable = !authService.isEmailExists(email);
        String message = isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        
        return ResponseEntity.ok(
                ApiResponse.success(message, isAvailable));
    }
}
