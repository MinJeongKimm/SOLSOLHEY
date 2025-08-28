package com.solsolhey.friend.service;

import com.solsolhey.friend.dto.request.FriendAddRequest;
import com.solsolhey.friend.dto.request.FriendInteractionRequest;
import com.solsolhey.friend.dto.response.*;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 친구 관리 서비스 인터페이스
 */
public interface FriendService {

    /**
     * 친구 요청 보내기
     */
    FriendResponse sendFriendRequest(User user, FriendAddRequest request);

    /**
     * 친구 요청 수락
     */
    FriendResponse acceptFriendRequest(User user, Long friendId);

    /**
     * 친구 요청 거절
     */
    void rejectFriendRequest(User user, Long friendId);

    /**
     * 친구 삭제
     */
    void removeFriend(User user, Long friendId);

    /**
     * 친구 목록 조회
     */
    Page<FriendResponse> getFriends(User user, Pageable pageable);

    /**
     * 받은 친구 요청 목록 조회
     */
    Page<FriendResponse> getPendingFriendRequests(User user, Pageable pageable);

    /**
     * 보낸 친구 요청 목록 조회
     */
    Page<FriendResponse> getSentFriendRequests(User user, Pageable pageable);

    /**
     * 친구 검색
     */
    List<FriendSearchResponse> searchFriends(User user, String keyword);

    /**
     * 친구 통계 조회
     */
    FriendStatsResponse getFriendStats(User user);

    /**
     * 친구 상호작용 보내기
     */
    FriendInteractionResponse sendInteraction(User user, FriendInteractionRequest request);

    /**
     * 받은 상호작용 목록 조회
     */
    Page<FriendInteractionResponse> getReceivedInteractions(User user, Pageable pageable);

    /**
     * 읽지 않은 상호작용 수 조회
     */
    Long getUnreadInteractionCount(User user);

    /**
     * 상호작용 읽음 처리
     */
    void markInteractionAsRead(User user, Long interactionId);

    /**
     * 친구 홈 데이터 집계 조회 (레벨/마스코트 요약 + 좋아요 누적/오늘 핑퐁 상태)
     */
    FriendHomeResponse getFriendHome(User viewer, Long friendId);
}
