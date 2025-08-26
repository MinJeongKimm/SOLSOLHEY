package com.solsolhey.point.service;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.common.exception.PointException;
import com.solsolhey.common.exception.EntityNotFoundException;
import com.solsolhey.point.dto.request.PointEarnRequest;
import com.solsolhey.point.dto.request.PointSpendRequest;
import com.solsolhey.point.dto.response.PointStatsResponse;
import com.solsolhey.point.dto.response.PointTransactionResponse;
import com.solsolhey.point.entity.PointTransaction;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import com.solsolhey.point.entity.PointTransaction.TransactionType;
import com.solsolhey.point.repository.PointTransactionRepository;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Integer DAILY_POINT_LIMIT = 100; // 일일 포인트 한도

    @Override
    public PointTransactionResponse earnPoints(User user, PointEarnRequest request) {
        log.debug("포인트 적립: userId={}, amount={}", user.getUserId(), request.pointAmount());

        // 일일 한도 확인
        if (!canEarnMorePointsToday(user, request.pointAmount())) {
            throw new BusinessException("일일 포인트 적립 한도를 초과했습니다.");
        }

        // 사용자 포인트 업데이트
        user.addPoints(request.pointAmount());
        userRepository.save(user);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .pointAmount(request.pointAmount())
                .transactionType(TransactionType.EARN)
                .description(request.description())
                .referenceId(request.referenceId())
                .referenceType(request.referenceType())
                .balanceAfter(user.getTotalPoints())
                .build();

        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        return PointTransactionResponse.from(savedTransaction);
    }

    @Override
    public PointTransactionResponse spendPoints(User user, PointSpendRequest request) {
        log.debug("포인트 사용: userId={}, amount={}", user.getUserId(), request.pointAmount());

        // 잔액 확인
        if (user.getTotalPoints() < request.pointAmount()) {
            throw new PointException.InsufficientPointsException(
                    user.getTotalPoints(), request.pointAmount()
            );
        }

        // 사용자 포인트 차감
        user.deductPoints(request.pointAmount());
        userRepository.save(user);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .pointAmount(request.pointAmount())
                .transactionType(TransactionType.SPEND)
                .description(request.description())
                .referenceId(request.referenceId())
                .referenceType(request.referenceType())
                .balanceAfter(user.getTotalPoints())
                .build();

        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        return PointTransactionResponse.from(savedTransaction);
    }

    @Override
    public PointTransactionResponse giveDailyBonus(User user) {
        log.debug("일일 보너스 지급: userId={}", user.getUserId());

        Integer bonusAmount = 10; // 일일 보너스 고정값
        String description = "일일 출석 보너스";

        // 오늘 이미 보너스를 받았는지 확인
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        List<PointTransaction> todayBonuses = pointTransactionRepository.findByUserAndCreatedAtBetween(
                user, startOfDay, endOfDay)
                .stream()
                .filter(t -> t.getReferenceType() == ReferenceType.DAILY_BONUS)
                .toList();

        if (!todayBonuses.isEmpty()) {
            throw new BusinessException("오늘 이미 일일 보너스를 받았습니다.");
        }

        // 포인트 지급
        user.addPoints(bonusAmount);
        userRepository.save(user);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .pointAmount(bonusAmount)
                .transactionType(TransactionType.BONUS)
                .description(description)
                .referenceType(ReferenceType.DAILY_BONUS)
                .balanceAfter(user.getTotalPoints())
                .build();

        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        return PointTransactionResponse.from(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointTransactionResponse> getUserTransactions(User user, Pageable pageable) {
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return transactions.map(PointTransactionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionResponse> getTransactionsByType(User user, String transactionType) {
        try {
            TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
            List<PointTransaction> transactions = pointTransactionRepository.findByUserAndTransactionType(user, type);
            return transactions.stream()
                    .map(PointTransactionResponse::from)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new BusinessException("잘못된 거래 타입입니다: " + transactionType);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionResponse> getTransactionsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<PointTransaction> transactions = pointTransactionRepository.findByUserAndCreatedAtBetween(
                user, startDateTime, endDateTime);
        
        return transactions.stream()
                .map(PointTransactionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PointStatsResponse getUserPointStats(User user) {
        Integer currentBalance = user.getTotalPoints();
        Integer totalEarned = pointTransactionRepository.getTotalEarnedPointsByUser(user);
        Integer totalSpent = pointTransactionRepository.getTotalSpentPointsByUser(user);
        Integer todayEarned = getTodayEarnedPoints(user);
        
        long totalTransactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, Pageable.unpaged())
                .getTotalElements();
        long earnTransactions = pointTransactionRepository.findByUserAndTransactionType(user, TransactionType.EARN).size();
        long spendTransactions = pointTransactionRepository.findByUserAndTransactionType(user, TransactionType.SPEND).size();

        return PointStatsResponse.of(currentBalance, totalEarned, totalSpent, todayEarned, 
                                   DAILY_POINT_LIMIT, totalTransactions, earnTransactions, spendTransactions);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, PointStatsResponse> getMonthlyStats(User user, int year) {
        List<Object[]> monthlyData = pointTransactionRepository.getMonthlyPointStatistics(user, year);
        Map<Integer, PointStatsResponse> monthlyStats = new HashMap<>();

        for (Object[] data : monthlyData) {
            Integer month = ((Number) data[0]).intValue();
            Integer earned = ((Number) data[1]).intValue();
            Integer spent = ((Number) data[2]).intValue();

            PointStatsResponse stats = PointStatsResponse.of(
                    0, earned, spent, 0, DAILY_POINT_LIMIT, 0L, 0L, 0L);
            monthlyStats.put(month, stats);
        }

        return monthlyStats;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTodayEarnedPoints(User user) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        return pointTransactionRepository.getTodayEarnedPointsByUser(user, startOfDay, endOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentBalance(User user) {
        return user.getTotalPoints();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionResponse> getTransactionsByReference(Long referenceId, ReferenceType referenceType) {
        List<PointTransaction> transactions = pointTransactionRepository.findByReferenceIdAndReferenceType(
                referenceId, referenceType);
        return transactions.stream()
                .map(PointTransactionResponse::from)
                .toList();
    }

    @Override
    public PointTransactionResponse refundPoints(User user, Long transactionId, String reason) {
        log.debug("포인트 환불: userId={}, transactionId={}", user.getUserId(), transactionId);

        PointTransaction originalTransaction = pointTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("거래 내역을 찾을 수 없습니다."));

        if (!originalTransaction.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("환불할 권한이 없습니다.");
        }

        if (originalTransaction.getTransactionType() != TransactionType.SPEND) {
            throw new BusinessException("사용 거래만 환불이 가능합니다.");
        }

        // 포인트 환불 처리
        user.addPoints(originalTransaction.getPointAmount());
        userRepository.save(user);

        // 환불 거래 내역 생성
        PointTransaction refundTransaction = PointTransaction.builder()
                .user(user)
                .pointAmount(originalTransaction.getPointAmount())
                .transactionType(TransactionType.REFUND)
                .description("환불: " + reason)
                .referenceId(transactionId)
                .referenceType(ReferenceType.ADMIN)
                .balanceAfter(user.getTotalPoints())
                .build();

        PointTransaction savedTransaction = pointTransactionRepository.save(refundTransaction);
        return PointTransactionResponse.from(savedTransaction);
    }

    @Override
    public PointTransactionResponse adminGivePoints(User user, Integer amount, String reason) {
        log.debug("관리자 포인트 지급: userId={}, amount={}", user.getUserId(), amount);

        user.addPoints(amount);
        userRepository.save(user);

        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .pointAmount(amount)
                .transactionType(TransactionType.BONUS)
                .description("관리자 지급: " + reason)
                .referenceType(ReferenceType.ADMIN)
                .balanceAfter(user.getTotalPoints())
                .build();

        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        return PointTransactionResponse.from(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEarnMorePointsToday(User user, Integer amount) {
        Integer todayEarned = getTodayEarnedPoints(user);
        return todayEarned + amount <= DAILY_POINT_LIMIT;
    }

    @Override
    public PointTransactionResponse awardChallengeReward(User user, Integer rewardPoints, Long challengeId) {
        log.debug("챌린지 완료 보상 지급: userId={}, challengeId={}, points={}", 
                  user.getUserId(), challengeId, rewardPoints);
        
        // 포인트 적립
        user.addPoints(rewardPoints);
        userRepository.save(user);
        
        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .pointAmount(rewardPoints)
                .transactionType(TransactionType.EARN)
                .description("챌린지 완료 보상")
                .referenceId(challengeId)
                .referenceType(ReferenceType.CHALLENGE)
                .balanceAfter(user.getTotalPoints())
                .build();
        
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        return PointTransactionResponse.from(savedTransaction);
    }
}
