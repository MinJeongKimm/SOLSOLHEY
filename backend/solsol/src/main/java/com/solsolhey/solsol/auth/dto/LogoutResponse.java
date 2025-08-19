package com.solsolhey.solsol.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그아웃 응답 DTO
 */
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogoutResponse {

    private boolean success;
    private String message;

    @Builder
    public LogoutResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * 성공 응답 생성
     */
    public static LogoutResponse success(String message) {
        return LogoutResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * 실패 응답 생성 (인증 실패)
     */
    public static LogoutResponse authFailure(String message) {
        return LogoutResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
