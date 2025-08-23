package com.solsolhey.auth.dto.response;

/**
 * 로그인 성공 응답 데이터 DTO
 */
public record LoginSuccessDto(
    String token,      // JWT 토큰
    String username    // 사용자명
) {
    /**
     * Compact canonical constructor for validation
     */
    public LoginSuccessDto {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰은 필수입니다.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("사용자명은 필수입니다.");
        }
    }
}
