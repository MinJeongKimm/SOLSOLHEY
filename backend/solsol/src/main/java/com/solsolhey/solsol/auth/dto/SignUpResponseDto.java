package com.solsolhey.solsol.auth.dto;

import com.solsolhey.solsol.entity.User;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponseDto {

    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private String campus;

    public static SignUpResponseDto from(User user) {
        return SignUpResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .campus(user.getCampus())
                .build();
    }
}
