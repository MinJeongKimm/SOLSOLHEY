package com.solsolhey.challenge.service;

import java.util.List;

import com.solsolhey.challenge.dto.response.*;
import com.solsolhey.challenge.dto.request.*;
import com.solsolhey.user.entity.User;

/**
 * 챌린지 관리 서비스 인터페이스
 */
public interface ChallengeService {

    /**
     * 챌린지 목록 조회
     */
    List<ChallengeListResponseDto> getChallenges(String category, User currentUser);

    /**
     * 챌린지 상세 조회
     */
    ChallengeDetailResponseDto getChallengeDetail(Long challengeId, User currentUser);

    /**
     * 챌린지 참여
     */
    ChallengeJoinResponseDto joinChallenge(Long challengeId, User user);

    /**
     * 챌린지 진행도 갱신
     */
    ChallengeProgressResponseDto updateProgress(Long challengeId, ChallengeProgressRequestDto request, User user);

    /**
     * 사용자의 참여 중인 챌린지 목록 조회
     */
    List<UserChallengeDto> getUserChallenges(User user, String status);

    /**
     * 만료된 챌린지 처리
     */
    void processExpiredChallenges();
}