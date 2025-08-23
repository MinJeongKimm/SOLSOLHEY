package com.solsolhey.point.service;

import com.solsolhey.point.dto.request.PointEarnRequest;
import com.solsolhey.point.dto.request.PointSpendRequest;
import com.solsolhey.point.dto.response.PointTransactionResponse;
import com.solsolhey.point.dto.response.PointStatsResponse;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 포인트 관리 서비스 인터페이스
 */
public interface PointService {

    /**
     * 포인트 적립
     */
    PointTransactionResponse earnPoints(User user, PointEarnRequest request);

    /**
     * 포인트 사용
     */
    PointTransactionResponse spendPoints(User user, PointSpendRequest request);

    /**
     * 일일 보너스 포인트 지급
     */
    PointTransactionResponse giveDailyBonus(User user);

    /**
     * 사용자 포인트 거래 내역 조회
     */
    Page<PointTransactionResponse> getUserTransactions(User user, Pageable pageable);

    /**
     * 특정 타입 거래 내역 조회
     */
    List<PointTransactionResponse> getTransactionsByType(User user, String transactionType);

    /**
     * 특정 기간 거래 내역 조회
     */
    List<PointTransactionResponse> getTransactionsByDateRange(User user, LocalDate startDate, LocalDate endDate);

    /**
     * 사용자 포인트 통계 조회
     */
    PointStatsResponse getUserPointStats(User user);

    /**
     * 월별 포인트 통계 조회
     */
    Map<Integer, PointStatsResponse> getMonthlyStats(User user, int year);

    /**
     * 오늘 획득한 포인트 조회
     */
    Integer getTodayEarnedPoints(User user);

    /**
     * 포인트 잔액 조회
     */
    Integer getCurrentBalance(User user);

    /**
     * 참조 ID로 거래 내역 조회
     */
    List<PointTransactionResponse> getTransactionsByReference(Long referenceId, ReferenceType referenceType);

    /**
     * 포인트 환불 (관리자용)
     */
    PointTransactionResponse refundPoints(User user, Long transactionId, String reason);

    /**
     * 관리자 포인트 지급
     */
    PointTransactionResponse adminGivePoints(User user, Integer amount, String reason);

    /**
     * 일일 포인트 한도 확인
     */
    boolean canEarnMorePointsToday(User user, Integer amount);
}
