package com.solsolhey.solsol.dto.friend;

import java.time.LocalDateTime;

import com.solsolhey.solsol.entity.Friend;
import com.solsolhey.solsol.entity.User;

import lombok.Builder;
import lombok.Getter;

/**
 * 친구 정보 응답 DTO
 */
@Getter
@Builder
public class FriendResponse {

    private final Long friendId;
    private final Long userId;
    private final String username;
    private final String nickname;
    private final String campus;
    private final Integer totalPoints;
    private final String status;
    private final LocalDateTime createdAt;
    
    // 마스코트 정보 (선택사항)
    private final Long mascotId;
    private final String mascotName;
    
    public static FriendResponse from(Friend friend) {
        User friendUser = friend.getFriendUser();
        
        return FriendResponse.builder()
                .friendId(friend.getFriendId())
                .userId(friendUser.getUserId())
                .username(friendUser.getUsername())
                .nickname(friendUser.getNickname())
                .campus(friendUser.getCampus())
                .totalPoints(friendUser.getTotalPoints())
                .status(friend.getStatus().name())
                .createdAt(friend.getCreatedAt())
                .build();
    }
    
    public static FriendResponse fromUser(User user) {
        return FriendResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .campus(user.getCampus())
                .totalPoints(user.getTotalPoints())
                .build();
    }
}
