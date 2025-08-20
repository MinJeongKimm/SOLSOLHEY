package com.solsolhey.solsol.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 현재 사용자 정보 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserInfoResponse {

    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private String campus;
    private Integer totalPoints;
    private Boolean isActive;

    /**
     * User 엔티티로부터 UserInfoResponse 생성
     */
    public static UserInfoResponse from(com.solsolhey.solsol.entity.User user) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .campus(user.getCampus())
                .totalPoints(user.getTotalPoints())
                .isActive(user.getIsActive())
                .build();
    }
}
