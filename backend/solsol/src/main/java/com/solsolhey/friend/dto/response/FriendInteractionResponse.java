package com.solsolhey.friend.dto.response;

import com.solsolhey.friend.entity.FriendInteraction;
import com.solsolhey.friend.entity.FriendInteraction.InteractionType;
import com.solsolhey.exp.service.ExpDailyCounterService;
import lombok.Builder;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 친구 상호작용 응답 DTO
 */
@Builder
public record FriendInteractionResponse(
    Long interactionId,
    Long fromUserId,
    String fromUserNickname,
    Long toUserId,
    String toUserNickname,
    InteractionType interactionType,
    String message,
    Long referenceId,
    Boolean isRead,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createdAt,
    ExpDailyCounterService.ExpAwarded expAwarded
) {
    public static FriendInteractionResponse from(FriendInteraction interaction) {
        return FriendInteractionResponse.builder()
                .interactionId(interaction.getInteractionId())
                .fromUserId(interaction.getFromUser().getUserId())
                .fromUserNickname(interaction.getFromUser().getNickname())
                .toUserId(interaction.getToUser().getUserId())
                .toUserNickname(interaction.getToUser().getNickname())
                .interactionType(interaction.getInteractionType())
                .message(interaction.getMessage())
                .referenceId(interaction.getReferenceId())
                .isRead(interaction.getIsRead())
                .createdAt(interaction.getCreatedAt())
                .expAwarded(null)
                .build();
    }
}
