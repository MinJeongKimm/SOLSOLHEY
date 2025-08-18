package com.solsolhey.solsol.auth.dto;

import com.solsolhey.solsol.entity.User;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private String campus;
    private Integer totalPoints;
    private TokenResponseDto tokens;

    public static LoginResponseDto from(User user, TokenResponseDto tokens) {
        return LoginResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .campus(user.getCampus())
                .totalPoints(user.getTotalPoints())
                .tokens(tokens)
                .build();
    }
}
