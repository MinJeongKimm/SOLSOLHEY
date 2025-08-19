package com.solsolhey.solsol.auth.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 응답 DTO (새로운 형식)
 */
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignUpResponse {

    private boolean success;
    private String message;
    private Long username; // 실제로는 생성된 사용자 ID
    private Map<String, String> errors; // 실패 시 필드별 에러

    @Builder
    public SignUpResponse(boolean success, String message, Long username, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.errors = errors;
    }

    /**
     * 성공 응답 생성
     */
    public static SignUpResponse success(String message, Long userId) {
        return SignUpResponse.builder()
                .success(true)
                .message(message)
                .username(userId)
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static SignUpResponse failure(String message, Map<String, String> errors) {
        return SignUpResponse.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
