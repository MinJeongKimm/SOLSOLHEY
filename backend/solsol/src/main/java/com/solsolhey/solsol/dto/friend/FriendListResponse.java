package com.solsolhey.solsol.dto.friend;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 친구 목록 응답 DTO
 */
@Getter
@Builder
public class FriendListResponse {

    private final List<FriendResponse> friends;
    private final Integer totalCount;
    private final Integer size;
    private final boolean hasNext;
    
    public static FriendListResponse of(List<FriendResponse> friends, Integer totalCount, 
                                       Integer requestedSize, boolean hasNext) {
        return FriendListResponse.builder()
                .friends(friends)
                .totalCount(totalCount)
                .size(requestedSize)
                .hasNext(hasNext)
                .build();
    }
}
