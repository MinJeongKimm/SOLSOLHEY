package com.solsolhey.challenge.service;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.common.exception.EntityNotFoundException;
import com.solsolhey.challenge.dto.response.*;
import com.solsolhey.challenge.dto.request.*;
import com.solsolhey.challenge.entity.*;
import com.solsolhey.challenge.repository.*;
import com.solsolhey.user.entity.User;
import com.solsolhey.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 챌린지 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeCategoryRepository categoryRepository;
    private final PointService pointService;
    private final com.solsolhey.exp.service.ExpDailyCounterService expDailyCounterService;

    /**
     * 챌린지 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChallengeListResponseDto> getChallenges(String category, User currentUser) {
        log.info("챌린지 목록 조회 시도: category={}, userId={}", category, currentUser.getUserId());

        List<Challenge> challenges;
        
        if (category != null && !category.isEmpty()) {
            // 카테고리별 조회
            ChallengeCategory.CategoryType categoryType;
            try {
                categoryType = ChallengeCategory.CategoryType.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("유효하지 않은 카테고리입니다: " + category);
            }
            
            Optional<ChallengeCategory> challengeCategory = categoryRepository.findByCategoryNameAndIsActiveTrue(categoryType);
            if (challengeCategory.isEmpty()) {
                throw new EntityNotFoundException("카테고리", category);
            }
            
            challenges = challengeRepository.findAvailableChallengesByCategory(challengeCategory.get(), LocalDateTime.now());
        } else {
            // 전체 조회
            challenges = challengeRepository.findAvailableChallenges(LocalDateTime.now());
        }

        // DTO 변환 및 사용자별 정보 설정
        List<ChallengeListResponseDto> result = challenges.stream()
                .map(challenge -> {
                    ChallengeListResponseDto dto = ChallengeListResponseDto.from(challenge);
                    
                    // 참여자 수 및 완료자 수 설정
                    Long participantCount = userChallengeRepository.countByChallenge(challenge);
                    Long completedCount = userChallengeRepository.countCompletedByChallenge(challenge);
                    dto.setParticipantInfo(participantCount, completedCount);
                    
                    // 현재 사용자의 참여 정보 설정
                    Optional<UserChallenge> userChallenge = userChallengeRepository.findByUserAndChallenge(currentUser, challenge);
                    if (userChallenge.isPresent()) {
                        dto.setUserInfo(true, userChallenge.get().getStatus().name());
                    } else {
                        dto.setUserInfo(false, "NOT_JOINED");
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("챌린지 목록 조회 완료: count={}", result.size());
        return result;
    }

    /**
     * 챌린지 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public ChallengeDetailResponseDto getChallengeDetail(Long challengeId, User currentUser) {
        log.info("챌린지 상세 조회 시도: challengeId={}, userId={}", challengeId, currentUser.getUserId());

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("챌린지", challengeId));

        // DTO 변환
        ChallengeDetailResponseDto response = ChallengeDetailResponseDto.from(challenge);

        // 통계 정보 설정
        Long participantCount = userChallengeRepository.countByChallenge(challenge);
        Long completedCount = userChallengeRepository.countCompletedByChallenge(challenge);
        response.setStatistics(participantCount, completedCount);

        // 현재 사용자의 참여 정보 설정
        Optional<UserChallenge> userChallenge = userChallengeRepository.findByUserAndChallenge(currentUser, challenge);
        response.setUserInfo(userChallenge.isPresent(), userChallenge.orElse(null));

        log.info("챌린지 상세 조회 완료: challengeId={}", challengeId);
        return response;
    }

    /**
     * 챌린지 참여
     */
    @Override
    public ChallengeJoinResponseDto joinChallenge(Long challengeId, User user) {
        log.info("챌린지 참여 시도: challengeId={}, userId={}", challengeId, user.getUserId());

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("챌린지", challengeId));

        // 챌린지 참여 가능 여부 확인
        if (!challenge.isAvailable()) {
            return ChallengeJoinResponseDto.failure("참여할 수 없는 챌린지입니다.");
        }

        // 이미 참여한 챌린지인지 확인
        if (userChallengeRepository.existsByUserAndChallenge(user, challenge)) {
            return ChallengeJoinResponseDto.failure("이미 참여한 챌린지입니다.");
        }

        // UserChallenge 생성
        UserChallenge userChallenge = UserChallenge.builder()
                .user(user)
                .challenge(challenge)
                .build();
        
        // 참여와 동시에 시작
        userChallenge.start();
        
        UserChallenge savedUserChallenge = userChallengeRepository.save(userChallenge);

        // EXP: 챌린지 참여 카테고리 1일 1회 +5 (마스코트 존재 시)
        java.util.Optional<com.solsolhey.exp.service.ExpDailyCounterService.ExpAwarded> catAwarded = java.util.Optional.empty();
        try {
            if (challenge.getCategory() != null) {
                catAwarded = expDailyCounterService.awardChallengeCategoryExp(user, challenge.getCategory().getCategoryName());
            }
        } catch (Exception e) {
            log.warn("챌린지 카테고리 EXP 적립 실패: userId={}, challengeId={}, err={}", user.getUserId(), challengeId, e.getMessage());
        }

        log.info("챌린지 참여 완료: challengeId={}, userId={}, userChallengeId={}", 
                challengeId, user.getUserId(), savedUserChallenge.getUserChallengeId());

        return ChallengeJoinResponseDto.builder()
                .success(true)
                .message("챌린지 참여가 완료되었습니다.")
                .userChallengeId(savedUserChallenge.getUserChallengeId())
                .status(savedUserChallenge.getStatus().name())
                .userChallenge(UserChallengeDto.from(savedUserChallenge))
                .expAwarded(catAwarded.orElse(null))
                .build();
    }

    /**
     * 챌린지 진행도 갱신
     */
    @Override
    public ChallengeProgressResponseDto updateProgress(Long challengeId, ChallengeProgressRequestDto request, User user) {
        log.info("챌린지 진행도 갱신 시도: challengeId={}, userId={}, step={}", 
                challengeId, user.getUserId(), request.getStep());

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("챌린지", challengeId));

        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new BusinessException("참여하지 않은 챌린지입니다."));

        // 진행 중인 챌린지인지 확인
        if (!userChallenge.isInProgress()) {
            return ChallengeProgressResponseDto.failure("진행 중인 챌린지가 아닙니다.");
        }

        boolean wasCompleted = userChallenge.isCompleted();

        // 진행도 업데이트 (FINANCE 카테고리 방어 로직) — 실제 금융 API 액션으로 인식되는 경우만 제한
        boolean isFinance = challenge.getCategory().getCategoryName() == ChallengeCategory.CategoryType.FINANCE;
        if (isFinance && isFinanceActionChallenge(challenge)) {
            String payload = request.getPayload();
            boolean validFinanceSuccess = payload != null && payload.startsWith("FINANCE_") && payload.endsWith("_SUCCESS");
            if (!validFinanceSuccess) {
                log.warn("금융 API형 챌린지 진행도 갱신 무시: payload 누락/무효. challengeId={}, userId={}, step={}, payload={}",
                        challengeId, user.getUserId(), request.getStepValue(), payload);
                return ChallengeProgressResponseDto.failure("외부 조회를 완료한 뒤에 완료할 수 있습니다.");
            }
        }

        userChallenge.updateProgress(request.getStepValue());
        
        // 추가 데이터가 있으면 업데이트
        if (request.getPayload() != null) {
            userChallenge.updateProgressData(request.getPayload());
        }

        UserChallenge savedUserChallenge = userChallengeRepository.save(userChallenge);

        // 챌린지가 새로 완료된 경우 보상 지급
        if (!wasCompleted && savedUserChallenge.isCompleted()) {
            try {
                pointService.awardChallengeReward(user, challenge.getRewardPoints(), challenge.getChallengeId());
                log.info("챌린지 완료 보상 지급: userId={}, challengeId={}, points={}", 
                        user.getUserId(), challengeId, challenge.getRewardPoints());
            } catch (Exception e) {
                log.error("챌린지 보상 지급 실패: userId={}, challengeId={}", user.getUserId(), challengeId, e);
                // 보상 지급 실패해도 진행도 업데이트는 성공으로 처리
            }
        }

        log.info("챌린지 진행도 갱신 완료: challengeId={}, userId={}, progress={}/{}, completed={}", 
                challengeId, user.getUserId(), savedUserChallenge.getProgressCount(), 
                savedUserChallenge.getTargetCount(), savedUserChallenge.isCompleted());

        return ChallengeProgressResponseDto.success(savedUserChallenge);
    }

    private boolean isFinanceActionChallenge(Challenge challenge) {
        String name = challenge.getChallengeName() != null ? challenge.getChallengeName().toLowerCase() : "";
        return name.contains("환율 전체") || name.contains("전체 환율") || name.contains("환율전체")
                || name.contains("환율 확인") || name.contains("단건") || name.contains("환율확인")
                || name.contains("환전") || name.contains("예상") || name.contains("환전예상")
                || name.contains("거래내역");
    }

    /**
     * 사용자의 참여 중인 챌린지 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserChallengeDto> getUserChallenges(User user, String status) {
        log.info("사용자 챌린지 목록 조회: userId={}, status={}", user.getUserId(), status);

        List<UserChallenge> userChallenges;
        
        if (status != null && !status.isEmpty()) {
            try {
                UserChallenge.ChallengeStatus challengeStatus = UserChallenge.ChallengeStatus.valueOf(status.toUpperCase());
                userChallenges = userChallengeRepository.findByUserAndStatusOrderByStartedAtDesc(user, challengeStatus);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("유효하지 않은 상태입니다: " + status);
            }
        } else {
            userChallenges = userChallengeRepository.findByUserOrderByStartedAtDesc(user);
        }

        return userChallenges.stream()
                .map(UserChallengeDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 만료된 챌린지 처리
     */
    @Override
    @Transactional
    public void processExpiredChallenges() {
        log.info("만료된 챌린지 처리 시작");
        
        LocalDateTime now = LocalDateTime.now();
        List<UserChallenge> expiredChallenges = userChallengeRepository.findExpiredInProgressChallenges(now);
        
        for (UserChallenge userChallenge : expiredChallenges) {
            userChallenge.expire();
            log.info("챌린지 만료 처리: userChallengeId={}, userId={}, challengeId={}", 
                    userChallenge.getUserChallengeId(), 
                    userChallenge.getUser().getUserId(), 
                    userChallenge.getChallenge().getChallengeId());
        }
        
        if (!expiredChallenges.isEmpty()) {
            userChallengeRepository.saveAll(expiredChallenges);
        }
        
        log.info("만료된 챌린지 처리 완료: count={}", expiredChallenges.size());
    }
}
