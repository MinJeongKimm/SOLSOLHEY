package com.solsolhey.solsol.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.solsol.auth.dto.LoginRequestDto;
import com.solsolhey.solsol.auth.dto.LoginResponse;
import com.solsolhey.solsol.auth.dto.LogoutResponse;
import com.solsolhey.solsol.auth.dto.RefreshTokenRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpResponse;
import com.solsolhey.solsol.auth.dto.TokenResponseDto;
import com.solsolhey.solsol.auth.service.AuthService;
import com.solsolhey.solsol.common.exception.BusinessException;
import com.solsolhey.solsol.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증/인가 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequestDto requestDto, 
            BindingResult bindingResult) {
        
        log.info("회원가입 요청: userId={}", requestDto.getUserId());
        
        // Validation 에러 처리
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                // userId 필드를 username으로 변환 (응답 명세에 맞춤)
                String fieldName = "userId".equals(error.getField()) ? "username" : error.getField();
                // 이메일 형식 에러 메시지 커스터마이징
                String errorMessage = error.getDefaultMessage();
                if ("올바른 이메일 형식이 아닙니다.".equals(errorMessage)) {
                    errorMessage = "이메일 형식이 올바르지 않습니다.";
                }
                errors.put(fieldName, errorMessage);
            }
            
            SignUpResponse response = SignUpResponse.failure("회원가입에 실패했습니다.", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        try {
            SignUpResponse response = authService.signUp(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (BusinessException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("username", e.getMessage());
            
            SignUpResponse response = SignUpResponse.failure("회원가입에 실패했습니다.", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            BindingResult bindingResult) {
        
        log.info("로그인 요청: userId={}", requestDto.getUserId());
        
        // Validation 에러 처리 (400 Bad Request)
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            
            LoginResponse response = LoginResponse.badRequest(
                "로그인 요청이 올바르지 않습니다. 아이디와 비밀번호를 모두 입력해주세요.", 
                errors
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // 로그인 처리
        LoginResponse response = authService.login(requestDto);
        
        // 응답 상태 코드 결정
        if (response.isSuccess()) {
            return ResponseEntity.ok(response); // 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
        }
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
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request) {
        
        log.info("로그아웃 요청");
        
        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("로그아웃 실패: Authorization 헤더가 없거나 잘못됨");
            LogoutResponse response = LogoutResponse.authFailure("인증에 실패했습니다. 토큰을 확인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String accessToken = authorizationHeader.substring(7);
            
            // 토큰 검증 (기본적인 검증)
            if (accessToken.trim().isEmpty()) {
                throw new IllegalArgumentException("토큰이 비어있습니다.");
            }
            
            authService.logout(accessToken);
            
            log.info("로그아웃 완료");
            LogoutResponse response = LogoutResponse.success("로그아웃 되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("로그아웃 실패: {}", e.getMessage());
            LogoutResponse response = LogoutResponse.authFailure("인증에 실패했습니다. 토큰을 확인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
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
