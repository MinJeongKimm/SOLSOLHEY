package com.solsolhey.solsol.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.solsol.auth.dto.CustomUserDetails;
import com.solsolhey.solsol.auth.dto.LoginRequestDto;
import com.solsolhey.solsol.auth.dto.LoginSuccessDto;
import com.solsolhey.solsol.auth.dto.RefreshTokenRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpResponse;
import com.solsolhey.solsol.auth.dto.TokenResponseDto;
import com.solsolhey.solsol.auth.dto.UserInfoResponse;
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
    // private final RateLimitingService rateLimitingService;  // 임시 비활성화

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
    public ResponseEntity<ApiResponse<LoginSuccessDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        log.info("로그인 요청: userId={}", requestDto.getUserId());
        
        // Validation 에러 처리 (400 Bad Request)
        if (bindingResult.hasErrors()) {
            ApiResponse<LoginSuccessDto> response = ApiResponse.error(HttpStatus.BAD_REQUEST,
                "로그인 요청이 올바르지 않습니다. 아이디와 비밀번호를 모두 입력해주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // 로그인 처리
        ApiResponse<LoginSuccessDto> response = authService.login(requestDto);
        
        // 응답 상태 코드 결정
        if (response.isSuccess()) {
            // 로그인 성공 시 Rate Limiting 보상 토큰 지급 (임시 비활성화)
            // String clientIp = getClientIpAddress(request);
            // rateLimitingService.onLoginSuccess(clientIp);
            // log.debug("로그인 성공 보상 토큰 지급: IP={}", clientIp);
            
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
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        
        log.info("로그아웃 요청");
        
        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("로그아웃 실패: Authorization 헤더가 없거나 잘못됨");
            ApiResponse<Void> response = ApiResponse.unauthorized("인증에 실패했습니다. 토큰을 확인해주세요.");
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
            ApiResponse<Void> response = ApiResponse.<Void>success();
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("로그아웃 실패: {}", e.getMessage());
            ApiResponse<Void> response = ApiResponse.unauthorized("인증에 실패했습니다. 토큰을 확인해주세요.");
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
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            log.info("현재 사용자 정보 조회: userId={}", userDetails.getUserId());
            
            // UserDetails에서 User 엔티티 가져오기
            UserInfoResponse userInfo = UserInfoResponse.from(userDetails.getUser());
            
            return ResponseEntity.ok(
                    ApiResponse.success("사용자 정보 조회 성공", userInfo));
                    
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 정보 조회에 실패했습니다."));
        }
    }
}
