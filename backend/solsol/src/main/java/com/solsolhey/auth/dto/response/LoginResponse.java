package com.solsolhey.auth.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO (새로운 형식)
 */
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private boolean success;
    private String message;
    private String token; // JWT 토큰
    private String username; // 로그인한 유저의 ID
    private Map<String, String> errors; // 실패 시 에러 정보

    @Builder
    public LoginResponse(boolean success, String message, String token, String username, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.username = username;
        this.errors = errors;
    }

    /**
     * 성공 응답 생성
     */
    public static LoginResponse success(String message, String token, String username) {
        return LoginResponse.builder()
                .success(true)
                .message(message)
                .token(token)
                .username(username)
                .build();
    }

    /**
     * 인증 실패 응답 생성
     */
    public static LoginResponse authFailure(String message) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 요청 오류 응답 생성
     */
    public static LoginResponse badRequest(String message, Map<String, String> errors) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
