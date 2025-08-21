package com.solsolhey.solsol.dto.ranking;

import lombok.Builder;
import lombok.Getter;

/**
 * 랭킹 응답 DTO
 */
@Getter
@Builder
public class RankingResponseDto {

    private Long entryId;
    private Long userId;
    private String nickname;
    private String campus;
    private Long mascotId;
    private String mascotName;
    private String mascotType;
    private String equippedItem;
    private Integer score;
    private Integer voteCount;
    private Integer rank;
    private String contestType;

    public static RankingResponseDto of(Long entryId, Long userId, String nickname, String campus,
                                      Long mascotId, String mascotName, String mascotType, String equippedItem,
                                      Integer score, Integer voteCount, Integer rank, String contestType) {
        return RankingResponseDto.builder()
                .entryId(entryId)
                .userId(userId)
                .nickname(nickname)
                .campus(campus)
                .mascotId(mascotId)
                .mascotName(mascotName)
                .mascotType(mascotType)
                .equippedItem(equippedItem)
                .score(score)
                .voteCount(voteCount)
                .rank(rank)
                .contestType(contestType)
                .build();
    }
}
