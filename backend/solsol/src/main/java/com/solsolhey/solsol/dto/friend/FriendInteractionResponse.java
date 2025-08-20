package com.solsolhey.solsol.dto.friend;

import java.time.LocalDateTime;

import com.solsolhey.solsol.entity.FriendInteraction;

import lombok.Builder;
import lombok.Getter;

/**
 * 친구 상호작용 응답 DTO
 */
@Getter
@Builder
public class FriendInteractionResponse {

    private final Long interactionId;
    private final Long targetUserId;
    private final String targetUsername;
    private final String targetNickname;
    private final String interactionType;
    private final Integer pointsEarned;
    private final LocalDateTime createdAt;
    private final boolean success;
    private final String message;
    
    public static FriendInteractionResponse from(FriendInteraction interaction, boolean success, String message) {
        return FriendInteractionResponse.builder()
                .interactionId(interaction.getInteractionId())
                .targetUserId(interaction.getTargetUser().getUserId())
                .targetUsername(interaction.getTargetUser().getUsername())
                .targetNickname(interaction.getTargetUser().getNickname())
                .interactionType(interaction.getInteractionType().name())
                .pointsEarned(interaction.getPointsEarned())
                .createdAt(interaction.getCreatedAt())
                .success(success)
                .message(message)
                .build();
    }
    
    public static FriendInteractionResponse success(FriendInteraction interaction, String message) {
        return from(interaction, true, message);
    }
    
    public static FriendInteractionResponse failure(Long targetUserId, String message) {
        return FriendInteractionResponse.builder()
                .targetUserId(targetUserId)
                .success(false)
                .message(message)
                .build();
    }
}
