package com.solsolhey.solsol.service;

import com.solsolhey.solsol.dto.friend.*;
import com.solsolhey.solsol.entity.User;

/**
 * 친구 관리 서비스 인터페이스
 */
public interface FriendService {

    /**
     * 친구 추가
     */
    FriendResponse addFriend(User currentUser, FriendAddRequest request);

    /**
     * 친구 삭제
     */
    void removeFriend(User currentUser, Long friendId);

    /**
     * 친구 목록 조회
     */
    FriendListResponse getFriendList(User currentUser, Integer size);

    /**
     * 친구 검색
     */
    FriendSearchResponse searchFriends(User currentUser, String query);

    /**
     * 사용자명으로 친구 조회
     */
    FriendResponse getFriendByUsername(User currentUser, String username);

    /**
     * 친구 상호작용 (좋아요, 방문, 응원)
     */
    FriendInteractionResponse interactWithFriend(User currentUser, FriendInteractionRequest request);

    /**
     * 친구 초대 코드 생성
     */
    String generateInvitationCode(User currentUser, Integer expiresInHours);

    /**
     * 초대 코드로 친구 추가
     */
    FriendResponse addFriendByInvitationCode(User currentUser, String invitationCode);

    /**
     * 사용자의 친구 통계 조회
     */
    FriendStatsResponse getFriendStats(User currentUser);

    /**
     * 상호 친구인지 확인
     */
    boolean areMutualFriends(User user1, User user2);

    /**
     * 친구 관계 존재 여부 확인
     */
    boolean areFriends(User user1, User user2);
}