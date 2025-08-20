package com.solsolhey.solsol.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.solsol.entity.PointTransaction;
import com.solsolhey.solsol.entity.User;
import com.solsolhey.solsol.repository.PointTransactionRepository;
import com.solsolhey.solsol.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 포인트 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PointServiceImpl implements PointService {

    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    /**
     * 포인트 적립
     */
    @Override
    public PointTransaction earnPoints(User user, Integer points, PointTransaction.PointSource source, 
                                     String description, Long referenceId) {
        log.info("포인트 적립 시도: userId={}, points={}, source={}", user.getUserId(), points, source);
        
        // 중복 지급 방지 (같은 소스와 참조 ID로 이미 지급된 경우)
        if (referenceId != null && pointTransactionRepository.existsByUserAndSourceAndReferenceId(user, source, referenceId)) {
            log.warn("이미 지급된 포인트: userId={}, source={}, referenceId={}", user.getUserId(), source, referenceId);
            throw new IllegalStateException("이미 지급된 보상입니다.");
        }

        // 포인트 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .transactionType(PointTransaction.TransactionType.EARN)
                .points(points)
                .source(source)
                .description(description)
                .referenceId(referenceId)
                .build();

        // 사용자 포인트 증가
        user.addPoints(points);
        
        // 데이터베이스에 저장
        userRepository.save(user);
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        
        log.info("포인트 적립 완료: userId={}, points={}, totalPoints={}", 
                user.getUserId(), points, user.getTotalPoints());
        
        return savedTransaction;
    }

    /**
     * 포인트 사용
     */
    @Override
    public PointTransaction spendPoints(User user, Integer points, PointTransaction.PointSource source,
                                      String description, Long referenceId) {
        log.info("포인트 사용 시도: userId={}, points={}, source={}", user.getUserId(), points, source);
        
        // 포인트 부족 확인
        if (user.getTotalPoints() < points) {
            throw new IllegalArgumentException("포인트가 부족합니다. (보유: " + user.getTotalPoints() + ", 필요: " + points + ")");
        }

        // 포인트 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .transactionType(PointTransaction.TransactionType.SPEND)
                .points(points)
                .source(source)
                .description(description)
                .referenceId(referenceId)
                .build();

        // 사용자 포인트 차감
        user.deductPoints(points);
        
        // 데이터베이스에 저장
        userRepository.save(user);
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        
        log.info("포인트 사용 완료: userId={}, points={}, totalPoints={}", 
                user.getUserId(), points, user.getTotalPoints());
        
        return savedTransaction;
    }

    /**
     * 포인트 환불
     */
    @Override
    public PointTransaction refundPoints(User user, Integer points, String description, Long referenceId) {
        log.info("포인트 환불 시도: userId={}, points={}", user.getUserId(), points);

        // 포인트 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .transactionType(PointTransaction.TransactionType.REFUND)
                .points(points)
                .source(PointTransaction.PointSource.REFUND)
                .description(description)
                .referenceId(referenceId)
                .build();

        // 사용자 포인트 증가
        user.addPoints(points);
        
        // 데이터베이스에 저장
        userRepository.save(user);
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        
        log.info("포인트 환불 완료: userId={}, points={}, totalPoints={}", 
                user.getUserId(), points, user.getTotalPoints());
        
        return savedTransaction;
    }

    /**
     * 사용자 포인트 거래 내역 조회 (페이징)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PointTransaction> getUserPointHistory(User user, Pageable pageable) {
        return pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * 사용자 포인트 거래 내역 조회 (전체)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PointTransaction> getUserPointHistory(User user) {
        return pointTransactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 특정 기간의 포인트 거래 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<PointTransaction> getUserPointHistoryInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return pointTransactionRepository.findByUserInPeriod(user, startDate, endDate);
    }

    /**
     * 사용자의 총 획득 포인트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Long getTotalEarnedPoints(User user) {
        return pointTransactionRepository.getTotalEarnedPoints(user);
    }

    /**
     * 사용자의 총 사용 포인트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Long getTotalSpentPoints(User user) {
        return pointTransactionRepository.getTotalSpentPoints(user);
    }

    /**
     * 특정 소스의 포인트 거래 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<PointTransaction> getPointHistoryBySource(User user, PointTransaction.PointSource source) {
        return pointTransactionRepository.findByUserAndSourceOrderByCreatedAtDesc(user, source);
    }

    /**
     * 챌린지 완료 보상 지급
     */
    @Override
    public PointTransaction awardChallengeReward(User user, Integer points, Long challengeId) {
        String description = "챌린지 완료 보상";
        return earnPoints(user, points, PointTransaction.PointSource.CHALLENGE, description, challengeId);
    }

    /**
     * 출석 보상 지급
     */
    @Override
    public PointTransaction awardAttendanceReward(User user, Integer points, String description) {
        return earnPoints(user, points, PointTransaction.PointSource.ATTENDANCE, description, null);
    }

    /**
     * 중복 지급 방지 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAlreadyRewarded(User user, PointTransaction.PointSource source, Long referenceId) {
        return pointTransactionRepository.existsByUserAndSourceAndReferenceId(user, source, referenceId);
    }
}
