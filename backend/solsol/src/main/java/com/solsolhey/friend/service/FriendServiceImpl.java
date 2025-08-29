package com.solsolhey.friend.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
import com.solsolhey.friend.dto.response.FriendHomeResponse;
import com.solsolhey.friend.entity.Friend;
import com.solsolhey.friend.entity.Friend.FriendshipStatus;
import com.solsolhey.friend.entity.FriendInteraction;
import com.solsolhey.friend.entity.FriendInteraction.InteractionType;
import com.solsolhey.friend.repository.FriendInteractionRepository;
import com.solsolhey.friend.repository.FriendRepository;
import com.solsolhey.friend.entity.FriendLikeDailyCounter;
import com.solsolhey.friend.repository.FriendLikeDailyCounterRepository;
import com.solsolhey.mascot.dto.view.MascotViewResponse;
import com.solsolhey.mascot.service.MascotViewService;
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

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final FriendRepository friendRepository;
    private final FriendInteractionRepository friendInteractionRepository;
    private final FriendLikeDailyCounterRepository likeDailyCounterRepository;
    private final UserRepository userRepository;
    private final ExpDailyCounterService expDailyCounterService;
    private final MascotViewService mascotViewService;

    @Override
    public FriendResponse sendFriendRequest(User user, FriendAddRequest request) {
        log.debug("친구 요청 보내기: userId={}, friendUserId={}", user.getUserId(), request.friendUserId());

        User friendUser = userRepository.findById(request.friendUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (user.getUserId().equals(friendUser.getUserId())) {
            throw new BusinessException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }

        // 기존 친구 관계/요청 상태 확인 후 처리 (중복/유물 레코드 정리 포함)
        List<Friend> all = friendRepository.findAllFriendshipsBetween(user, friendUser);
        if (!all.isEmpty()) {
            // ACCEPTED가 하나라도 있으면 새 요청 불가
            boolean anyAccepted = all.stream().anyMatch(f -> f.getStatus() == FriendshipStatus.ACCEPTED);
            if (anyAccepted) {
                throw new BusinessException("이미 친구입니다.");
            }

            // 최신 레코드를 기준으로 재요청 허용 처리, 나머지는 정리
            all.sort((a, b) -> Long.compare(
                    b.getFriendId() == null ? 0L : b.getFriendId(),
                    a.getFriendId() == null ? 0L : a.getFriendId()
            ));
            Friend base = all.get(0);
            // base를 요청 방향(user -> friendUser)으로 설정하고 PENDING으로 전환
            base.setUser(user);
            base.setFriendUser(friendUser);
            base.setStatus(FriendshipStatus.PENDING);
            Friend revived = friendRepository.save(base);

            // 여분 레코드는 삭제하여 상태 일관성 보장
            for (int i = 1; i < all.size(); i++) {
                try { friendRepository.delete(all.get(i)); } catch (Exception ignore) {}
            }

            // 친구 요청 알림 상호작용 생성
            try {
                String msg = user.getNickname() + "님이 친구 요청을 보냈습니다.";
                FriendInteraction reqNotice = FriendInteraction.builder()
                        .fromUser(user)
                        .toUser(friendUser)
                        .interactionType(InteractionType.FRIEND_REQUEST)
                        .message(msg)
                        .referenceId(revived.getFriendId())
                        .build();
                friendInteractionRepository.save(reqNotice);
            } catch (Exception e) {
                log.warn("친구 요청 알림 생성 실패(revive-multi): from={}, to={}, err={}", user.getUserId(), friendUser.getUserId(), e.getMessage());
            }

            return FriendResponse.from(revived, false);
        }

        Friend friend = Friend.builder()
                .user(user)
                .friendUser(friendUser)
                .status(FriendshipStatus.PENDING)
                .build();

        Friend savedFriend = friendRepository.save(friend);

        // 친구 요청 알림 상호작용 생성: 수신자(friendUser)에게 FRIEND_REQUEST 타입 알림
        try {
            String msg = user.getNickname() + "님이 친구 요청을 보냈습니다.";
            FriendInteraction reqNotice = FriendInteraction.builder()
                    .fromUser(user)
                    .toUser(friendUser)
                    .interactionType(InteractionType.FRIEND_REQUEST)
                    .message(msg)
                    .referenceId(savedFriend.getFriendId())
                    .build();
            friendInteractionRepository.save(reqNotice);
        } catch (Exception e) {
            log.warn("친구 요청 알림 생성 실패: from={}, to={}, err={}", user.getUserId(), friendUser.getUserId(), e.getMessage());
        }

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

        // 수신자(user) 관점의 친구요청 알림 메시지 갱신 + 읽음 처리
        try {
            List<FriendInteraction> notices = friendInteractionRepository
                    .findByToUserAndTypeAndReferenceId(user, InteractionType.FRIEND_REQUEST, friendId);
            if (!notices.isEmpty()) {
                FriendInteraction notice = notices.get(0);
                String fromNickname = friend.getUser().getNickname();
                notice.setMessage("이제 " + fromNickname + "님과 친구가 되었습니다.");
                notice.markAsRead();
            }
        } catch (Exception e) {
            log.warn("친구요청 수락 알림 갱신 실패: toUser={}, friendId={}, err={}", user.getUserId(), friendId, e.getMessage());
        }

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

        // 수신자(user) 관점의 친구요청 알림 삭제(인박스에서 사라짐)
        try {
            List<FriendInteraction> notices = friendInteractionRepository
                    .findByToUserAndTypeAndReferenceId(user, InteractionType.FRIEND_REQUEST, friendId);
            for (FriendInteraction n : notices) {
                friendInteractionRepository.delete(n);
            }
        } catch (Exception e) {
            log.warn("친구요청 거절 알림 삭제 실패: toUser={}, friendId={}, err={}", user.getUserId(), friendId, e.getMessage());
        }
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

        if (request.interactionType() == InteractionType.LIKE) {
            LocalDate today = LocalDate.now(KST);
            // 통합 카운터(경로 무관) - 비관적 락으로 동시성 제어
            FriendLikeDailyCounter counter = likeDailyCounterRepository
                    .findForUpdate(user, toUser, today)
                    .orElseGet(() -> new FriendLikeDailyCounter(user, toUser, today));
            Integer current = counter.getLikeCount() == null ? 0 : counter.getLikeCount();
            if (current >= 3) {
                throw new BusinessException("오늘 해당 친구에게 보낼 수 있는 좋아요 3회 한도를 초과했습니다.");
            }
            counter.inc();
            likeDailyCounterRepository.save(counter);
        }

        // 기본 메시지: 타입별 디폴트 메시지를 제공 (요청 본문이 없을 때)
        String msg = request.message();
        if ((msg == null || msg.isBlank()) && request.interactionType() == InteractionType.LIKE) {
            msg = user.getNickname() + "님이 좋아요를 보냈습니다.";
        }

        FriendInteraction interaction = FriendInteraction.builder()
                .fromUser(user)
                .toUser(toUser)
                .interactionType(request.interactionType())
                .message(msg)
                .build();

        FriendInteraction savedInteraction = friendInteractionRepository.save(interaction);
        Optional<ExpDailyCounterService.ExpAwarded> activeAwarded = Optional.empty();
        try {
            // 방문자(발신자): active 규칙(1~3회 +3, 이후 +1)
            activeAwarded = expDailyCounterService.awardFriendInteractionExpActive(user);
        } catch (Exception e) {
            log.warn("친구 Active EXP 적립 실패: userId={}, err={}", user.getUserId(), e.getMessage());
        }
        try {
            // 피방문자(수신자): passive 규칙(+1, 무제한)
            expDailyCounterService.awardFriendInteractionExpPassive(toUser);
        } catch (Exception e) {
            log.warn("친구 Passive EXP 적립 실패: toUserId={}, err={}", toUser.getUserId(), e.getMessage());
        }
        
        return FriendInteractionResponse.builder()
                .interactionId(savedInteraction.getInteractionId())
                .fromUserId(savedInteraction.getFromUser().getUserId())
                .fromUserNickname(savedInteraction.getFromUser().getNickname())
                .toUserId(savedInteraction.getToUser().getUserId())
                .toUserNickname(savedInteraction.getToUser().getNickname())
                .interactionType(savedInteraction.getInteractionType())
                .message(savedInteraction.getMessage())
                .referenceId(savedInteraction.getReferenceId())
                .isRead(savedInteraction.getIsRead())
                .createdAt(savedInteraction.getCreatedAt())
                .expAwarded(activeAwarded.orElse(null))
                .build();
    }

    @Override
    public FriendInteractionResponse likeBack(User user, Long interactionId) {
        // 원본 알림 확인
        FriendInteraction original = friendInteractionRepository.findById(interactionId)
                .orElseThrow(() -> new EntityNotFoundException("상호작용을 찾을 수 없습니다."));

        if (!original.getToUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("해당 상호작용에 답장할 권한이 없습니다.");
        }
        if (original.getInteractionType() != InteractionType.LIKE) {
            throw new BusinessException("좋아요에만 답장할 수 있습니다.");
        }

        // 친구 여부 검증 (양방향)
        User target = original.getFromUser();
        if (!friendRepository.existsMutualFriendship(user, target)) {
            throw new BusinessException("친구 관계가 아닌 사용자입니다.");
        }

        // 기존 sendInteraction 로직 재사용 (핑퐁 규칙 포함)
        FriendInteractionRequest req = new FriendInteractionRequest(
                target.getUserId(), InteractionType.LIKE, null
        );
        FriendInteractionResponse sent = sendInteraction(user, req);

        // 원본 알림 읽음 처리
        try {
            original.markAsRead();
        } catch (Exception e) {
            log.warn("좋아요 답장 후 원본 읽음처리 실패: interactionId={}, err={}", interactionId, e.getMessage());
        }

        return sent;
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

    @Override
    public void markAllReceivedInteractionsAsRead(User user) {
        log.debug("상호작용 모두 읽음 처리: userId={}", user.getUserId());
        friendInteractionRepository.markAllAsReadByToUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendHomeResponse getFriendHome(User viewer, Long friendId) {
        User owner = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        boolean self = viewer.getUserId().equals(owner.getUserId());
        boolean isFriend = self || friendRepository.existsMutualFriendship(viewer, owner);
        if (!isFriend) {
            throw new BusinessException("친구 관계가 아닌 사용자입니다.");
        }

        // Mascot view summary
        MascotViewResponse view = mascotViewService.getView(viewer.getUserId(), owner.getUserId());

        // Lifetime like count to owner (from anyone)
        long lifetimeLikes = friendInteractionRepository
                .countByToUserAndInteractionType(owner, InteractionType.LIKE);

        // Today (KST) unified count via counter
        LocalDate today = LocalDate.now(KST);
        int sentToday = likeDailyCounterRepository.findOne(viewer, owner, today)
                .map(FriendLikeDailyCounter::getLikeCount)
                .orElse(0);
        int allowedMax = 3;
        int remaining = Math.max(0, allowedMax - sentToday);
        boolean canLikeNow = remaining > 0;

        return FriendHomeResponse.builder()
                .ownerId(view.getOwner().getId())
                .nickname(view.getOwner().getNickname())
                .level(view.getOwner().getLevel())
                .likeCount(lifetimeLikes)
                .likeSentToday((int) sentToday)
                .likeReceivedToday((int) receivedToday)
                .likeAllowedMax(allowedMax)
                .likeRemainingToday(remaining)
                .canLikeNow(canLikeNow)
                .viewMode(view.getViewMode())
                .permissions(view.getPermissions())
                .mascot(view.getMascot())
                .isFriend(isFriend)
                .isOwner(self)
                .build();
    }
}
