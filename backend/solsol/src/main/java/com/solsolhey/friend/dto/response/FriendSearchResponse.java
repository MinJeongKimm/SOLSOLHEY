package com.solsolhey.friend.dto.response;

import com.solsolhey.user.entity.User;
import lombok.Builder;

/**
 * 친구 검색 결과 DTO
 */
@Builder
public record FriendSearchResponse(
    Long userId,
    String nickname,
    String email,
    String campus,
    Integer totalPoints,
    Boolean isAlreadyFriend,
    Boolean hasPendingRequest
) {
    public static FriendSearchResponse from(User user, Boolean isAlreadyFriend, Boolean hasPendingRequest) {
        return FriendSearchResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .campus(user.getCampus())
                .totalPoints(user.getTotalPoints())
                .isAlreadyFriend(isAlreadyFriend)
                .hasPendingRequest(hasPendingRequest)
                .build();
    }
}
