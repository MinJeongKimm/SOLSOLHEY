package com.solsolhey.solsol.dto.friend;

import java.util.List;

import com.solsolhey.solsol.entity.User;

import lombok.Builder;
import lombok.Getter;

/**
 * 친구 검색 응답 DTO
 */
@Getter
@Builder
public class FriendSearchResponse {

    private final List<FriendSearchResult> results;
    private final Integer totalCount;
    private final String query;
    
    @Getter
    @Builder
    public static class FriendSearchResult {
        private final Long userId;
        private final String username;
        private final String nickname;
        private final String campus;
        private final Integer totalPoints;
        private final boolean isFriend; // 이미 친구인지 여부
        private final boolean isCurrentUser; // 현재 사용자 본인인지 여부
        
        public static FriendSearchResult fromUser(User user, boolean isFriend, boolean isCurrentUser) {
            return FriendSearchResult.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .campus(user.getCampus())
                    .totalPoints(user.getTotalPoints())
                    .isFriend(isFriend)
                    .isCurrentUser(isCurrentUser)
                    .build();
        }
    }
    
    public static FriendSearchResponse of(List<FriendSearchResult> results, String query) {
        return FriendSearchResponse.builder()
                .results(results)
                .totalCount(results.size())
                .query(query)
                .build();
    }
}
