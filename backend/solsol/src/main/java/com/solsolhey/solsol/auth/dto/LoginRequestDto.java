package com.solsolhey.solsol.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "사용자명 또는 이메일은 필수입니다.")
    private String usernameOrEmail;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
