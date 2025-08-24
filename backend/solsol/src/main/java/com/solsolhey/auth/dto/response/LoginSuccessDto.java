package com.solsolhey.auth.dto.response;

/**
 * 로그인 성공 응답 데이터 DTO
 */
public record LoginSuccessDto(
    String token,      // JWT 토큰
    Long userId        // 사용자 ID
) {
    /**
     * Compact canonical constructor for validation
     */
    public LoginSuccessDto {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰은 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
    }
}
