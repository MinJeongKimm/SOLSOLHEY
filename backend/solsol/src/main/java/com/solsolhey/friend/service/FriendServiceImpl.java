package com.solsolhey.friend.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.common.exception.EntityNotFoundException;
import com.solsolhey.friend.dto.request.FriendAddRequest;
import com.solsolhey.friend.dto.request.FriendInteractionRequest;
import com.solsolhey.friend.dto.response.FriendInteractionResponse;
import com.solsolhey.friend.dto.response.FriendResponse;
import com.solsolhey.friend.dto.response.FriendSearchResponse;
import com.solsolhey.friend.dto.response.FriendStatsResponse;
import com.solsolhey.friend.entity.Friend;
import com.solsolhey.friend.entity.Friend.FriendshipStatus;
import com.solsolhey.friend.entity.FriendInteraction;
import com.solsolhey.friend.repository.FriendInteractionRepository;
import com.solsolhey.friend.repository.FriendRepository;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;
import com.solsolhey.exp.service.ExpDailyCounterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 친구 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final FriendInteractionRepository friendInteractionRepository;
    private final UserRepository userRepository;
    private final ExpDailyCounterService expDailyCounterService;

    @Override
    public FriendResponse sendFriendRequest(User user, FriendAddRequest request) {
        log.debug("친구 요청 보내기: userId={}, friendUserId={}", user.getUserId(), request.friendUserId());

        User friendUser = userRepository.findById(request.friendUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (user.getUserId().equals(friendUser.getUserId())) {
            throw new BusinessException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }

        // 이미 친구 관계가 있는지 확인
        if (friendRepository.findFriendshipBetween(user, friendUser).isPresent()) {
            throw new BusinessException("이미 친구 요청이 존재합니다.");
        }

        Friend friend = Friend.builder()
                .user(user)
                .friendUser(friendUser)
                .status(FriendshipStatus.PENDING)
                .build();

        Friend savedFriend = friendRepository.save(friend);
        
        return FriendResponse.from(savedFriend, false);
    }

    @Override
    public FriendResponse acceptFriendRequest(User user, Long friendId) {
        log.debug("친구 요청 수락: userId={}, friendId={}", user.getUserId(), friendId);

        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("친구 요청을 찾을 수 없습니다."));

        if (!friend.getFriendUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("친구 요청을 수락할 권한이 없습니다.");
        }

        if (friend.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessException("대기 중인 친구 요청이 아닙니다.");
        }

        friend.accept();
        return FriendResponse.from(friend, true);
    }

    @Override
    public void rejectFriendRequest(User user, Long friendId) {
        log.debug("친구 요청 거절: userId={}, friendId={}", user.getUserId(), friendId);

        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("친구 요청을 찾을 수 없습니다."));

        if (!friend.getFriendUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("친구 요청을 거절할 권한이 없습니다.");
        }

        friend.reject();
    }

    @Override
    public void removeFriend(User user, Long friendId) {
        log.debug("친구 삭제: userId={}, friendId={}", user.getUserId(), friendId);

        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("친구 관계를 찾을 수 없습니다."));

        if (!friend.getUser().getUserId().equals(user.getUserId()) && 
            !friend.getFriendUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("친구를 삭제할 권한이 없습니다.");
        }

        friendRepository.delete(friend);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FriendResponse> getFriends(User user, Pageable pageable) {
        Page<Friend> friends = friendRepository.findAcceptedFriends(user, FriendshipStatus.ACCEPTED, pageable);
        return friends.map(friend -> {
            boolean isRequestReceivedByMe = friend.getFriendUser().getUserId().equals(user.getUserId());
            return FriendResponse.from(friend, isRequestReceivedByMe);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FriendResponse> getPendingFriendRequests(User user, Pageable pageable) {
        Page<Friend> requests = friendRepository.findByFriendUserAndStatus(user, FriendshipStatus.PENDING, pageable);
        return requests.map(friend -> FriendResponse.from(friend, true));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FriendResponse> getSentFriendRequests(User user, Pageable pageable) {
        Page<Friend> requests = friendRepository.findByUserAndStatus(user, FriendshipStatus.PENDING, pageable);
        return requests.map(friend -> FriendResponse.from(friend, false));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendSearchResponse> searchFriends(User user, String keyword) {
        log.debug("친구 검색: userId={}, keyword={}", user.getUserId(), keyword);
        
        List<User> users = userRepository.findByNickname(keyword);
        
        return users.stream()
                .filter(u -> !u.getUserId().equals(user.getUserId()))
                .map(u -> {
                    boolean isAlreadyFriend = friendRepository.existsMutualFriendship(user, u);
                    boolean hasPendingRequest = friendRepository.findFriendshipBetween(user, u)
                            .map(f -> f.getStatus() == FriendshipStatus.PENDING)
                            .orElse(false);
                    
                    return FriendSearchResponse.from(u, isAlreadyFriend, hasPendingRequest);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FriendStatsResponse getFriendStats(User user) {
        Long totalFriends = friendRepository.countFriendsByUser(user);
        
        Long pendingRequests = (long) friendRepository.findByFriendUserAndStatus(
                user, FriendshipStatus.PENDING, Pageable.unpaged()).getContent().size();
        
        Long sentRequests = (long) friendRepository.findByUserAndStatus(
                user, FriendshipStatus.PENDING, Pageable.unpaged()).getContent().size();
        
        Long unreadInteractions = friendInteractionRepository.countUnreadByToUser(user);
        
        LocalDateTime startOfDay = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MAX);
        Long todayInteractions = friendInteractionRepository.countTodayInteractionsByToUser(
                user, startOfDay, endOfDay);

        return FriendStatsResponse.of(totalFriends, pendingRequests, sentRequests, 
                                    unreadInteractions, todayInteractions);
    }

    @Override
    public FriendInteractionResponse sendInteraction(User user, FriendInteractionRequest request) {
        log.debug("상호작용 보내기: userId={}, toUserId={}, type={}", 
                 user.getUserId(), request.toUserId(), request.interactionType());

        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 친구 관계 확인
        if (!friendRepository.existsMutualFriendship(user, toUser)) {
            throw new BusinessException("친구 관계가 아닌 사용자에게는 상호작용을 보낼 수 없습니다.");
        }

        FriendInteraction interaction = FriendInteraction.builder()
                .fromUser(user)
                .toUser(toUser)
                .interactionType(request.interactionType())
                .message(request.message())
                .build();

        FriendInteraction savedInteraction = friendInteractionRepository.save(interaction);
        try {
            // 방문자(발신자): active 규칙(1~3회 +3, 이후 +1)
            expDailyCounterService.awardFriendInteractionExpActive(user);
        } catch (Exception e) {
            log.warn("친구 Active EXP 적립 실패: userId={}, err={}", user.getUserId(), e.getMessage());
        }
        try {
            // 피방문자(수신자): passive 규칙(+1, 무제한)
            expDailyCounterService.awardFriendInteractionExpPassive(toUser);
        } catch (Exception e) {
            log.warn("친구 Passive EXP 적립 실패: toUserId={}, err={}", toUser.getUserId(), e.getMessage());
        }
        
        return FriendInteractionResponse.from(savedInteraction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FriendInteractionResponse> getReceivedInteractions(User user, Pageable pageable) {
        Page<FriendInteraction> interactions = friendInteractionRepository
                .findByToUserOrderByCreatedAtDesc(user, pageable);
        return interactions.map(FriendInteractionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadInteractionCount(User user) {
        return friendInteractionRepository.countUnreadByToUser(user);
    }

    @Override
    public void markInteractionAsRead(User user, Long interactionId) {
        FriendInteraction interaction = friendInteractionRepository.findById(interactionId)
                .orElseThrow(() -> new EntityNotFoundException("상호작용을 찾을 수 없습니다."));

        if (!interaction.getToUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("상호작용을 읽을 권한이 없습니다.");
        }

        interaction.markAsRead();
    }
}
