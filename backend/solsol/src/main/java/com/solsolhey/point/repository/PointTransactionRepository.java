package com.solsolhey.point.repository;

import com.solsolhey.point.entity.PointTransaction;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import com.solsolhey.point.entity.PointTransaction.TransactionType;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 거래내역 Repository
 */
@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    /**
     * 사용자별 포인트 거래내역 조회 (최신순)
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user ORDER BY pt.createdAt DESC")
    Page<PointTransaction> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    /**
     * 사용자별 특정 거래타입 내역 조회
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = :type " +
           "ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByUserAndTransactionType(@Param("user") User user, 
                                                        @Param("type") TransactionType type);

    /**
     * 특정 기간 동안의 포인트 거래내역 조회
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user AND " +
           "pt.createdAt BETWEEN :startDate AND :endDate ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByUserAndCreatedAtBetween(@Param("user") User user,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 총 적립 포인트 조회
     */
    @Query("SELECT COALESCE(SUM(pt.pointAmount), 0) FROM PointTransaction pt WHERE pt.user = :user AND " +
           "pt.transactionType IN ('EARN', 'BONUS')")
    Integer getTotalEarnedPointsByUser(@Param("user") User user);

    /**
     * 사용자의 총 사용 포인트 조회
     */
    @Query("SELECT COALESCE(SUM(pt.pointAmount), 0) FROM PointTransaction pt WHERE pt.user = :user AND " +
           "pt.transactionType = 'SPEND'")
    Integer getTotalSpentPointsByUser(@Param("user") User user);

    /**
     * 특정 참조 ID와 타입으로 거래내역 조회
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.referenceId = :referenceId AND " +
           "pt.referenceType = :referenceType")
    List<PointTransaction> findByReferenceIdAndReferenceType(@Param("referenceId") Long referenceId,
                                                             @Param("referenceType") ReferenceType referenceType);

    /**
     * 오늘 획득한 포인트 합계
     */
    @Query("SELECT COALESCE(SUM(pt.pointAmount), 0) FROM PointTransaction pt WHERE pt.user = :user AND " +
           "pt.transactionType IN ('EARN', 'BONUS') AND " +
           "pt.createdAt >= :startOfDay AND pt.createdAt < :endOfDay")
    Integer getTodayEarnedPointsByUser(@Param("user") User user, 
                                      @Param("startOfDay") LocalDateTime startOfDay,
                                      @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 월별 포인트 통계
     */
    @Query("SELECT EXTRACT(MONTH FROM pt.createdAt) as month, " +
           "SUM(CASE WHEN pt.transactionType IN ('EARN', 'BONUS') THEN pt.pointAmount ELSE 0 END) as earned, " +
           "SUM(CASE WHEN pt.transactionType = 'SPEND' THEN pt.pointAmount ELSE 0 END) as spent " +
           "FROM PointTransaction pt WHERE pt.user = :user AND " +
           "EXTRACT(YEAR FROM pt.createdAt) = :year " +
           "GROUP BY EXTRACT(MONTH FROM pt.createdAt) " +
           "ORDER BY month")
    List<Object[]> getMonthlyPointStatistics(@Param("user") User user, @Param("year") int year);
}
