package com.solsolhey.auth.service;

import com.solsolhey.auth.dto.request.LoginRequestDto;
import com.solsolhey.auth.dto.response.LoginSuccessDto;
import com.solsolhey.auth.dto.request.SignUpRequestDto;
import com.solsolhey.auth.dto.response.SignUpResponse;
import com.solsolhey.auth.dto.response.TokenResponseDto;
import com.solsolhey.common.response.ApiResponse;

/**
 * 인증/인가 관련 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 회원가입
     */
    SignUpResponse signUp(SignUpRequestDto requestDto);

    /**
     * 로그인
     */
    ApiResponse<LoginSuccessDto> login(LoginRequestDto requestDto);

    /**
     * 토큰 갱신
     */
    TokenResponseDto refreshToken(String refreshToken);

    /**
     * 로그아웃 (토큰 블랙리스트 처리)
     * 토큰을 블랙리스트에 추가하여 재사용을 방지
     */
    void logout(String accessToken);

    /**
     * 사용자명 중복 체크 (userId는 이메일 형식이므로 이메일 중복 체크)
     */
    boolean isUsernameExists(String username);

    /**
     * 이메일 중복 체크 (userId 중복 체크와 동일)
     */
    boolean isEmailExists(String email);
}