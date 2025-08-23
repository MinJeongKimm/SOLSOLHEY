package com.solsolhey.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 업데이트 요청 DTO
 */
public record UserUpdateRequest(
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
    String nickname
) {
    public UserUpdateRequest {
        if (nickname != null && nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 공백일 수 없습니다");
        }
    }
}
