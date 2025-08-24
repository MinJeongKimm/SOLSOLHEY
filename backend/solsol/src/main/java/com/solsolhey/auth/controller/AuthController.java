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
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;

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
    private final com.solsolhey.auth.jwt.JwtTokenProvider jwtTokenProvider;

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
    @Operation(summary = "로그인", description = "사용자 인증을 수행하고 JWT 토큰을 쿠키로 설정합니다")
    public ResponseEntity<ApiResponse<LoginSuccessDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            log.info("로그인 API 호출: userId={}", requestDto.userId());
            ApiResponse<LoginSuccessDto> result = authService.login(requestDto);
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            // 1) Issue tokens
            String userId = result.getData().userId().toString(); // adapt if your DTO differs
            String access = jwtTokenProvider.createAccessToken(Long.parseLong(userId));
            String refresh = jwtTokenProvider.createRefreshToken(Long.parseLong(userId));

            ResponseCookie accessC = ResponseCookie.from("ACCESS_TOKEN", access)
                .httpOnly(true).secure(false)           // TODO: prod=true
                .sameSite("Lax").path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

            ResponseCookie refreshC = ResponseCookie.from("REFRESH_TOKEN", refresh)
                .httpOnly(true).secure(false)           // TODO: prod=true
                .sameSite("Lax").path("/api/v1/auth")
                .maxAge(Duration.ofDays(14))
                .build();

            // 2) CSRF (XSRF-TOKEN as readable cookie)
            // Spring Security의 자동 CSRF 토큰 생성에 의존
            // csrfRepo.generateToken(request);
            // csrfRepo.saveToken(csrf, request, response);

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessC.toString())
                .header(HttpHeaders.SET_COOKIE, refreshC.toString())
                .body(result); // Consider removing token fields from body if present
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 수행하고 쿠키를 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        
        try {
            log.info("로그아웃 API 호출");
            
            ResponseCookie clearAccess = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(0).build();
            ResponseCookie clearRefresh = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/api/v1/auth").maxAge(0).build();
            ResponseCookie clearX = ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false).secure(false).sameSite("Lax").path("/").maxAge(0).build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAccess.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefresh.toString())
                .header(HttpHeaders.SET_COOKIE, clearX.toString())
                .body(ApiResponse.success("로그아웃이 완료되었습니다.", null));
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰 발급
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급합니다")
    public ResponseEntity<Void> refresh(
            @CookieValue(value="REFRESH_TOKEN", required=false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken).toString();

        String newAccess = jwtTokenProvider.createAccessToken(Long.parseLong(userId));
        String newRefresh = jwtTokenProvider.createRefreshToken(Long.parseLong(userId));

        ResponseCookie accessC = ResponseCookie.from("ACCESS_TOKEN", newAccess)
            .httpOnly(true).secure(false).sameSite("Lax").path("/")
            .maxAge(Duration.ofMinutes(15)).build();

        ResponseCookie refreshC = ResponseCookie.from("REFRESH_TOKEN", newRefresh)
            .httpOnly(true).secure(false).sameSite("Lax").path("/api/v1/auth")
            .maxAge(Duration.ofDays(14)).build();

        // Re-issue CSRF (optional but recommended on rotation)
        // Spring Security의 자동 CSRF 토큰 생성에 의존
        // CsrfToken csrf = csrfRepo.generateToken(request);
        // csrfRepo.saveToken(csrf, request, response);

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, accessC.toString())
            .header(HttpHeaders.SET_COOKIE, refreshC.toString())
            .build();
    }

    /**
     * 세션 확인
     */
    @GetMapping("/me")
    @Operation(summary = "세션 확인", description = "현재 로그인한 사용자 정보를 반환합니다(인증 필요).")
    public ResponseEntity<ApiResponse<Map<String,Object>>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다."));
        }
        String principal = String.valueOf(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", Map.of(
            "username", principal
            // 필요 시 userId/nickname은 실제 값으로 채우거나 생략
        )));
    }
}
