package com.solsolhey.solsol.service;

import com.solsolhey.solsol.common.exception.BusinessException;
import com.solsolhey.solsol.common.exception.EntityNotFoundException;
import com.solsolhey.solsol.dto.friend.*;
import com.solsolhey.solsol.entity.*;
import com.solsolhey.solsol.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 친구 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final FriendInvitationRepository friendInvitationRepository;
    private final FriendInteractionRepository friendInteractionRepository;
    private final UserRepository userRepository;
    private final PointService pointService;

    // 상호작용별 포인트 상수
    private static final int LIKE_POINTS = 1;
    private static final int VISIT_POINTS = 2;
    private static final int CHEER_POINTS = 3;
    private static final int FRIEND_INVITE_POINTS = 10;

    @Override
    public FriendResponse addFriend(User currentUser, FriendAddRequest request) {
        log.info("친구 추가 시도: userId={}, request={}", currentUser.getUserId(), request);

        request.validate();

        User targetUser = findTargetUser(request);
        
        // 자기 자신을 친구로 추가하려는 경우 확인
        if (targetUser.getUserId().equals(currentUser.getUserId())) {
            throw new BusinessException("자기 자신을 친구로 추가할 수 없습니다.");
        }

        // 이미 친구인지 확인
        if (friendRepository.existsByUserAndFriendUser(currentUser, targetUser)) {
            throw new BusinessException("이미 친구로 추가된 사용자입니다.");
        }

        // 친구 관계 생성
        Friend friend = Friend.builder()
                .user(currentUser)
                .friendUser(targetUser)
                .build();

        Friend savedFriend = friendRepository.save(friend);

        // 초대 코드로 친구 추가한 경우 보상 지급
        if (request.getCode() != null) {
            try {
                pointService.earnPoints(currentUser, FRIEND_INVITE_POINTS, 
                    PointTransaction.PointSource.FRIEND_INVITE, 
                    "친구 초대 보상", targetUser.getUserId());
                log.info("친구 초대 보상 지급: userId={}, points={}", currentUser.getUserId(), FRIEND_INVITE_POINTS);
            } catch (Exception e) {
                log.warn("친구 초대 보상 지급 실패: userId={}", currentUser.getUserId(), e);
            }
        }

        log.info("친구 추가 완료: userId={}, friendUserId={}", currentUser.getUserId(), targetUser.getUserId());
        return FriendResponse.from(savedFriend);
    }

    @Override
    public void removeFriend(User currentUser, Long friendId) {
        log.info("친구 삭제 시도: userId={}, friendId={}", currentUser.getUserId(), friendId);

        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("친구", friendId));

        // 본인의 친구인지 확인
        if (!friend.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new BusinessException("삭제 권한이 없습니다.");
        }

        friendRepository.delete(friend);
        log.info("친구 삭제 완료: userId={}, friendId={}", currentUser.getUserId(), friendId);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendListResponse getFriendList(User currentUser, Integer size) {
        log.info("친구 목록 조회: userId={}, size={}", currentUser.getUserId(), size);

        if (size == null || size <= 0) {
            size = 20; // 기본값
        }

        Pageable pageable = PageRequest.of(0, size);
        Page<Friend> friendPage = friendRepository.findByUserAndStatusOrderByCreatedAtDesc(
                currentUser, Friend.FriendStatus.ACTIVE, pageable);

        List<FriendResponse> friends = friendPage.getContent().stream()
                .map(FriendResponse::from)
                .collect(Collectors.toList());

        Long totalCount = friendRepository.countByUserAndStatus(currentUser, Friend.FriendStatus.ACTIVE);

        return FriendListResponse.of(friends, totalCount.intValue(), size, friendPage.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public FriendSearchResponse searchFriends(User currentUser, String query) {
        log.info("친구 검색: userId={}, query={}", currentUser.getUserId(), query);

        if (query == null || query.trim().isEmpty()) {
            return FriendSearchResponse.of(List.of(), query);
        }

        String searchQuery = query.trim();
        
        // 사용자명이나 닉네임으로 검색
        List<User> searchResults = userRepository.findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(
                searchQuery, searchQuery);

        List<FriendSearchResponse.FriendSearchResult> results = searchResults.stream()
                .map(user -> {
                    boolean isFriend = friendRepository.existsByUserAndFriendUserAndStatus(
                            currentUser, user, Friend.FriendStatus.ACTIVE);
                    boolean isCurrentUser = user.getUserId().equals(currentUser.getUserId());
                    
                    return FriendSearchResponse.FriendSearchResult.fromUser(user, isFriend, isCurrentUser);
                })
                .collect(Collectors.toList());

        return FriendSearchResponse.of(results, searchQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendResponse getFriendByUsername(User currentUser, String username) {
        log.info("사용자명으로 친구 조회: userId={}, username={}", currentUser.getUserId(), username);

        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자", username));

        return FriendResponse.fromUser(targetUser);
    }

    @Override
    public FriendInteractionResponse interactWithFriend(User currentUser, FriendInteractionRequest request) {
        log.info("친구 상호작용: userId={}, targetUserId={}, type={}", 
                currentUser.getUserId(), request.getTargetUserId(), request.getInteractionType());

        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자", request.getTargetUserId()));

        // 자기 자신과는 상호작용할 수 없음
        if (targetUser.getUserId().equals(currentUser.getUserId())) {
            return FriendInteractionResponse.failure(request.getTargetUserId(), "자기 자신과는 상호작용할 수 없습니다.");
        }

        Date today = Date.valueOf(LocalDate.now());
        
        // 오늘 이미 같은 타입의 상호작용을 했는지 확인
        if (friendInteractionRepository.existsByUserAndTargetUserAndInteractionTypeAndDate(
                currentUser, targetUser, request.getInteractionType(), today)) {
            return FriendInteractionResponse.failure(request.getTargetUserId(), "오늘 이미 해당 상호작용을 했습니다.");
        }

        // 상호작용 포인트 계산
        int points = calculateInteractionPoints(request.getInteractionType());

        // 상호작용 기록 생성
        FriendInteraction interaction = FriendInteraction.builder()
                .user(currentUser)
                .targetUser(targetUser)
                .interactionType(request.getInteractionType())
                .date(today)
                .pointsEarned(points)
                .build();

        FriendInteraction savedInteraction = friendInteractionRepository.save(interaction);

        // 포인트 지급
        try {
            pointService.earnPoints(currentUser, points, 
                PointTransaction.PointSource.FRIEND_INTERACTION,
                request.getInteractionType().name() + " 상호작용",
                savedInteraction.getInteractionId());
        } catch (Exception e) {
            log.warn("상호작용 포인트 지급 실패: interactionId={}", savedInteraction.getInteractionId(), e);
        }

        String message = getInteractionSuccessMessage(request.getInteractionType(), points);
        return FriendInteractionResponse.success(savedInteraction, message);
    }

    @Override
    public String generateInvitationCode(User currentUser, Integer expiresInHours) {
        log.info("초대 코드 생성: userId={}, expiresInHours={}", currentUser.getUserId(), expiresInHours);

        if (expiresInHours == null || expiresInHours <= 0) {
            expiresInHours = 24; // 기본 24시간
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expiresInHours);

        FriendInvitation invitation = FriendInvitation.builder()
                .inviter(currentUser)
                .expiresAt(expiresAt)
                .build();

        FriendInvitation savedInvitation = friendInvitationRepository.save(invitation);

        log.info("초대 코드 생성 완료: userId={}, code={}, expiresAt={}", 
                currentUser.getUserId(), savedInvitation.getInvitationCode(), expiresAt);

        return savedInvitation.getInvitationCode();
    }

    @Override
    public FriendResponse addFriendByInvitationCode(User currentUser, String invitationCode) {
        log.info("초대 코드로 친구 추가: userId={}, code={}", currentUser.getUserId(), invitationCode);

        FriendInvitation invitation = friendInvitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new EntityNotFoundException("초대 코드", invitationCode));

        // 초대 수락 처리
        invitation.accept(currentUser);
        friendInvitationRepository.save(invitation);

        // 친구 추가 (중복 체크는 addFriend 메서드에서 처리)
        FriendAddRequest addRequest = new FriendAddRequest(invitation.getInviter().getUserId());
        return addFriend(currentUser, addRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendStatsResponse getFriendStats(User currentUser) {
        log.info("친구 통계 조회: userId={}", currentUser.getUserId());

        // 총 친구 수
        Long totalFriendsCount = friendRepository.countByUserAndStatus(currentUser, Friend.FriendStatus.ACTIVE);
        
        // 상호 친구 수
        List<User> mutualFriends = friendRepository.findMutualFriends(currentUser);
        
        // 오늘 상호작용 수
        Date today = Date.valueOf(LocalDate.now());
        List<FriendInteraction> todayInteractions = friendInteractionRepository.findByUserAndDateOrderByCreatedAtDesc(currentUser, today);
        
        // 총 상호작용 수
        Long totalInteractions = (long) friendInteractionRepository.findByUserOrderByCreatedAtDesc(currentUser).size();
        
        // 받은 상호작용별 통계
        Long likesReceived = friendInteractionRepository.countByTargetUserAndInteractionType(currentUser, FriendInteraction.InteractionType.LIKE);
        Long visitsReceived = friendInteractionRepository.countByTargetUserAndInteractionType(currentUser, FriendInteraction.InteractionType.VISIT);
        Long cheersReceived = friendInteractionRepository.countByTargetUserAndInteractionType(currentUser, FriendInteraction.InteractionType.CHEER);
        
        // 친구 상호작용으로 얻은 포인트 (오늘 기준)
        Integer pointsEarnedToday = friendInteractionRepository.getTotalPointsEarnedToday(currentUser, today);

        return FriendStatsResponse.of(
                totalFriendsCount.intValue(),
                mutualFriends.size(),
                todayInteractions.size(),
                totalInteractions.intValue(),
                likesReceived.intValue(),
                visitsReceived.intValue(),
                cheersReceived.intValue(),
                pointsEarnedToday != null ? pointsEarnedToday : 0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areMutualFriends(User user1, User user2) {
        return friendRepository.existsByUserAndFriendUserAndStatus(user1, user2, Friend.FriendStatus.ACTIVE) &&
               friendRepository.existsByUserAndFriendUserAndStatus(user2, user1, Friend.FriendStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areFriends(User user1, User user2) {
        return friendRepository.existsByUserAndFriendUserAndStatus(user1, user2, Friend.FriendStatus.ACTIVE);
    }

    private User findTargetUser(FriendAddRequest request) {
        if (request.getCode() != null) {
            // 초대 코드로 친구 추가
            FriendInvitation invitation = friendInvitationRepository.findByInvitationCode(request.getCode())
                    .orElseThrow(() -> new EntityNotFoundException("초대 코드", request.getCode()));
            
            if (!invitation.isUsable()) {
                throw new BusinessException("유효하지 않거나 만료된 초대 코드입니다.");
            }
            
            return invitation.getInviter();
        } else if (request.getUserId() != null) {
            // 사용자 ID로 친구 추가
            return userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("사용자", request.getUserId()));
        } else if (request.getUsername() != null) {
            // 사용자명으로 친구 추가
            return userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("사용자", request.getUsername()));
        }
        
        throw new BusinessException("친구 추가 정보가 올바르지 않습니다.");
    }

    private int calculateInteractionPoints(FriendInteraction.InteractionType type) {
        return switch (type) {
            case LIKE -> LIKE_POINTS;
            case VISIT -> VISIT_POINTS;
            case CHEER -> CHEER_POINTS;
        };
    }

    private String getInteractionSuccessMessage(FriendInteraction.InteractionType type, int points) {
        return switch (type) {
            case LIKE -> "좋아요를 보냈습니다! +" + points + " 포인트";
            case VISIT -> "친구를 방문했습니다! +" + points + " 포인트";
            case CHEER -> "응원을 보냈습니다! +" + points + " 포인트";
        };
    }
}