package com.solsolhey.friend.dto.response;

import lombok.Builder;

/**
 * 친구 통계 응답 DTO
 */
@Builder
public record FriendStatsResponse(
    Long totalFriends,
    Long pendingRequests,
    Long sentRequests,
    Long unreadInteractions,
    Long todayInteractions
) {
    public static FriendStatsResponse of(Long totalFriends, Long pendingRequests, 
                                        Long sentRequests, Long unreadInteractions, 
                                        Long todayInteractions) {
        return FriendStatsResponse.builder()
                .totalFriends(totalFriends)
                .pendingRequests(pendingRequests)
                .sentRequests(sentRequests)
                .unreadInteractions(unreadInteractions)
                .todayInteractions(todayInteractions)
                .build();
    }
}
