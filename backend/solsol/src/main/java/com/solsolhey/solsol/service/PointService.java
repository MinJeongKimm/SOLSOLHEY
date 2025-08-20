package com.solsolhey.solsol.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.solsolhey.solsol.entity.PointTransaction;
import com.solsolhey.solsol.entity.User;

/**
 * 포인트 관리 서비스 인터페이스
 */
public interface PointService {

    /**
     * 포인트 적립
     */
    PointTransaction earnPoints(User user, Integer points, PointTransaction.PointSource source, 
                              String description, Long referenceId);

    /**
     * 포인트 사용
     */
    PointTransaction spendPoints(User user, Integer points, PointTransaction.PointSource source,
                               String description, Long referenceId);

    /**
     * 포인트 환불
     */
    PointTransaction refundPoints(User user, Integer points, String description, Long referenceId);

    /**
     * 사용자 포인트 거래 내역 조회 (페이징)
     */
    Page<PointTransaction> getUserPointHistory(User user, Pageable pageable);

    /**
     * 사용자 포인트 거래 내역 조회 (전체)
     */
    List<PointTransaction> getUserPointHistory(User user);

    /**
     * 특정 기간의 포인트 거래 내역 조회
     */
    List<PointTransaction> getUserPointHistoryInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 사용자의 총 획득 포인트 조회
     */
    Long getTotalEarnedPoints(User user);

    /**
     * 사용자의 총 사용 포인트 조회
     */
    Long getTotalSpentPoints(User user);

    /**
     * 특정 소스의 포인트 거래 내역 조회
     */
    List<PointTransaction> getPointHistoryBySource(User user, PointTransaction.PointSource source);

    /**
     * 챌린지 완료 보상 지급
     */
    PointTransaction awardChallengeReward(User user, Integer points, Long challengeId);

    /**
     * 출석 보상 지급
     */
    PointTransaction awardAttendanceReward(User user, Integer points, String description);

    /**
     * 중복 지급 방지 확인
     */
    boolean isAlreadyRewarded(User user, PointTransaction.PointSource source, Long referenceId);
}