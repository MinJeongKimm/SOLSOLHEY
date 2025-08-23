package com.solsolhey.friend.dto.response;

import com.solsolhey.friend.entity.Friend;
import com.solsolhey.friend.entity.Friend.FriendshipStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 친구 정보 응답 DTO
 */
@Builder
public record FriendResponse(
    Long friendId,
    Long userId,
    String nickname,
    String email,
    String campus,
    FriendshipStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FriendResponse from(Friend friend, boolean isRequestReceiver) {
        var targetUser = isRequestReceiver ? friend.getUser() : friend.getFriendUser();
        
        return FriendResponse.builder()
                .friendId(friend.getFriendId())
                .userId(targetUser.getUserId())
                .nickname(targetUser.getNickname())
                .email(targetUser.getEmail())
                .campus(targetUser.getCampus())
                .status(friend.getStatus())
                .createdAt(friend.getCreatedAt())
                .updatedAt(friend.getUpdatedAt())
                .build();
    }
}
