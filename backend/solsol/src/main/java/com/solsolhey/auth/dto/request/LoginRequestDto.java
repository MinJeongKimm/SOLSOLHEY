package com.solsolhey.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 */
public record LoginRequestDto(
    @NotBlank(message = "userId는 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String userId,
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {
    public LoginRequestDto {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
    }
}
