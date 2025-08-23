package com.solsolhey.friend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 친구 추가 요청 DTO
 */
public record FriendAddRequest(
    @NotNull(message = "친구 사용자 ID는 필수입니다.")
    Long friendUserId,
    
    @Size(max = 200, message = "메시지는 200자 이하여야 합니다.")
    String message
) {
    public FriendAddRequest {
        if (friendUserId == null) {
            throw new IllegalArgumentException("친구 사용자 ID는 필수입니다.");
        }
    }
}
