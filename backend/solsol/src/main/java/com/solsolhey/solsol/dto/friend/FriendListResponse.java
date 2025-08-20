package com.solsolhey.solsol.dto.friend;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 친구 목록 응답 DTO
 */
@Getter
@Builder
public class FriendListResponse {

    private List<FriendResponse> friends;
    private Integer totalCount;
    private Integer size;
    private boolean hasNext;
    
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
