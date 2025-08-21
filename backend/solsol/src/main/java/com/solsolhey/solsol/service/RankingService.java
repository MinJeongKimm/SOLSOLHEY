package com.solsolhey.solsol.service;

import com.solsolhey.solsol.dto.ranking.RankingResponseDto;
import com.solsolhey.solsol.dto.ranking.VoteRequestDto;
import com.solsolhey.solsol.dto.ranking.VoteResponseDto;
import com.solsolhey.solsol.entity.ContestEntry;

import java.util.List;

/**
 * 랭킹 서비스 인터페이스
 */
public interface RankingService {

    /**
     * 교내 랭킹 조회
     */
    List<RankingResponseDto> getCampusRankings(String sort);

    /**
     * 전국 랭킹 조회
     */
    List<RankingResponseDto> getNationalRankings(String sort);

    /**
     * 전국 랭킹 투표
     */
    VoteResponseDto voteForNationalRanking(Long entryId, Long voterId);

    /**
     * 콘테스트 참가
     */
    ContestEntry participateInContest(Long userId, Long mascotId, ContestEntry.ContestType contestType);

    /**
     * 특정 콘테스트 타입의 랭킹 조회
     */
    List<RankingResponseDto> getRankingsByType(ContestEntry.ContestType contestType, String sort);
}
