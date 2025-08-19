package com.solsolhey.solsol.auth.dto;

import jakarta.validation.constraints.Email;
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

    @NotBlank(message = "userId는 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
