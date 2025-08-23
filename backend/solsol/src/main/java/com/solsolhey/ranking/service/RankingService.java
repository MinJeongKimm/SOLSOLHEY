package com.solsolhey.ranking.service;

import com.solsolhey.ranking.dto.request.CampusRankingRequest;
import com.solsolhey.ranking.dto.request.NationalRankingRequest;
import com.solsolhey.ranking.dto.request.VoteRequest;
import com.solsolhey.ranking.dto.response.RankingResponse;
import com.solsolhey.ranking.dto.response.VoteResponse;
import com.solsolhey.ranking.entity.ContestEntry;

/**
 * 랭킹 서비스 인터페이스
 */
public interface RankingService {

    /**
     * 교내 랭킹 조회
     */
    RankingResponse getCampusRankings(CampusRankingRequest request, String userCampus);

    /**
     * 전국 랭킹 조회
     */
    RankingResponse getNationalRankings(NationalRankingRequest request);

    /**
     * 교내 랭킹 투표
     */
    VoteResponse voteForCampus(Long entryId, VoteRequest request, Long voterId, String userCampus);

    /**
     * 전국 랭킹 투표
     */
    VoteResponse voteForNational(Long entryId, VoteRequest request, Long voterId);

    /**
     * 콘테스트 참가 (마스코트 등록)
     */
    ContestEntry participateInContest(Long userId, Long mascotId, ContestEntry.ContestType contestType);

    /**
     * 투표 가능 여부 확인
     */
    boolean canVote(Long voterId, Long entryId, ContestEntry.ContestType contestType);

    /**
     * 일일 투표 한도 확인
     */
    boolean hasReachedDailyVoteLimit(Long voterId, ContestEntry.ContestType contestType);

    /**
     * 중복 투표 확인
     */
    boolean hasAlreadyVoted(Long voterId, Long entryId);

    /**
     * 멱등키 중복 확인
     */
    boolean isDuplicateIdempotencyKey(String idempotencyKey);

    /**
     * 콘테스트 엔트리 조회
     */
    ContestEntry getContestEntry(Long entryId);

    /**
     * 사용자별 참가 엔트리 조회
     */
    java.util.List<ContestEntry> getUserEntries(Long userId, ContestEntry.ContestType contestType);
}
