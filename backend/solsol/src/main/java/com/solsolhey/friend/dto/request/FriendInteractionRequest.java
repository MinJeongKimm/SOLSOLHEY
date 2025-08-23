package com.solsolhey.friend.dto.request;

import com.solsolhey.friend.entity.FriendInteraction.InteractionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 친구 상호작용 요청 DTO
 */
public record FriendInteractionRequest(
    @NotNull(message = "대상 사용자 ID는 필수입니다.")
    Long toUserId,
    
    @NotNull(message = "상호작용 타입은 필수입니다.")
    InteractionType interactionType,
    
    @Size(max = 200, message = "메시지는 200자 이하여야 합니다.")
    String message
) {
    public FriendInteractionRequest {
        if (toUserId == null) {
            throw new IllegalArgumentException("대상 사용자 ID는 필수입니다.");
        }
        if (interactionType == null) {
            throw new IllegalArgumentException("상호작용 타입은 필수입니다.");
        }
    }
}
