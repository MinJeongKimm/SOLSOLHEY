package com.solsolhey.ranking.service;

import java.util.List;

import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.ranking.dto.request.CampusRankingRequest;
import com.solsolhey.ranking.dto.request.NationalRankingRequest;
import com.solsolhey.ranking.dto.request.VoteRequest;
import com.solsolhey.ranking.dto.response.RankingResponse;
import com.solsolhey.ranking.dto.response.VoteResponse;
import com.solsolhey.ranking.entity.Vote;

/**
 * 랭킹 서비스 인터페이스 (마스코트 기반)
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
     * 투표 가능 여부 확인
     */
    boolean canVote(Long voterId, Long entryId, Vote.VoteType voteType);

    /**
     * 일일 투표 한도 확인
     */
    boolean hasReachedDailyVoteLimit(Long voterId, Vote.VoteType voteType);

    // 투표 타입별로 중복 투표 체크하므로 이 메서드는 더 이상 사용하지 않음
    // boolean hasAlreadyVoted(Long voterId, Long mascotId);

    /**
     * 멱등키 중복 확인
     */
    boolean isDuplicateIdempotencyKey(String idempotencyKey);

    /**
     * 마스코트 조회
     */
    Mascot getMascotById(Long mascotId);

    /**
     * 사용자의 교내 랭킹 투표 히스토리 조회 (투표한 마스코트 ID 목록)
     */
    List<Long> getUserVotedMascotIdsForCampus(Long voterId);

    /**
     * 사용자의 전국 랭킹 투표 히스토리 조회 (투표한 마스코트 ID 목록)
     */
    List<Long> getUserVotedMascotIdsForNational(Long voterId);
}
