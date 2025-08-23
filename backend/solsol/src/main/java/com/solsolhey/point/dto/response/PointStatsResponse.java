package com.solsolhey.point.dto.response;

import lombok.Builder;

/**
 * 포인트 통계 응답 DTO
 */
@Builder
public record PointStatsResponse(
    Integer currentBalance,
    Integer totalEarned,
    Integer totalSpent,
    Integer todayEarned,
    Integer todayLimit,
    Integer remainingTodayLimit,
    Long totalTransactions,
    Long earnTransactions,
    Long spendTransactions,
    Double averagePerTransaction
) {
    public static PointStatsResponse of(Integer currentBalance, Integer totalEarned, Integer totalSpent,
                                       Integer todayEarned, Integer todayLimit, Long totalTransactions,
                                       Long earnTransactions, Long spendTransactions) {
        Integer remainingTodayLimit = Math.max(0, todayLimit - todayEarned);
        Double averagePerTransaction = totalTransactions > 0 ? 
                (double) totalEarned / totalTransactions : 0.0;

        return PointStatsResponse.builder()
                .currentBalance(currentBalance)
                .totalEarned(totalEarned)
                .totalSpent(totalSpent)
                .todayEarned(todayEarned)
                .todayLimit(todayLimit)
                .remainingTodayLimit(remainingTodayLimit)
                .totalTransactions(totalTransactions)
                .earnTransactions(earnTransactions)
                .spendTransactions(spendTransactions)
                .averagePerTransaction(Math.round(averagePerTransaction * 100.0) / 100.0)
                .build();
    }
}
