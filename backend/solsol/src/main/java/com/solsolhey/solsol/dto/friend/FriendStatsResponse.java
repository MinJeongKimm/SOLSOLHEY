package com.solsolhey.solsol.dto.friend;

import lombok.Builder;
import lombok.Getter;

/**
 * 친구 통계 응답 DTO
 */
@Getter
@Builder
public class FriendStatsResponse {

    private final Integer totalFriends;      // 총 친구 수
    private final Integer mutualFriends;     // 상호 친구 수
    private final Integer todayInteractions; // 오늘 상호작용 수
    private final Integer totalInteractions; // 총 상호작용 수
    private final Integer likesReceived;     // 받은 좋아요 수
    private final Integer visitsReceived;    // 받은 방문 수
    private final Integer cheersReceived;    // 받은 응원 수
    private final Integer pointsEarnedFromFriends; // 친구 상호작용으로 얻은 포인트
    
    public static FriendStatsResponse of(Integer totalFriends, Integer mutualFriends, 
                                        Integer todayInteractions, Integer totalInteractions,
                                        Integer likesReceived, Integer visitsReceived, 
                                        Integer cheersReceived, Integer pointsEarnedFromFriends) {
        return FriendStatsResponse.builder()
                .totalFriends(totalFriends)
                .mutualFriends(mutualFriends)
                .todayInteractions(todayInteractions)
                .totalInteractions(totalInteractions)
                .likesReceived(likesReceived)
                .visitsReceived(visitsReceived)
                .cheersReceived(cheersReceived)
                .pointsEarnedFromFriends(pointsEarnedFromFriends)
                .build();
    }
}
