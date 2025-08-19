package com.solsolhey.solsol.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
