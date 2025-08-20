package com.solsolhey.solsol.dto.friend;

import com.solsolhey.solsol.entity.FriendInteraction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 상호작용 요청 DTO
 */
@Getter
@NoArgsConstructor
public class FriendInteractionRequest {

    @NotNull(message = "대상 사용자 ID는 필수입니다.")
    private Long targetUserId;

    @NotNull(message = "상호작용 타입은 필수입니다.")
    private FriendInteraction.InteractionType interactionType;

    public FriendInteractionRequest(Long targetUserId, FriendInteraction.InteractionType interactionType) {
        this.targetUserId = targetUserId;
        this.interactionType = interactionType;
    }
}
