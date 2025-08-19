package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.PointTransaction;
import com.solsolhey.solsol.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 거래 내역 레포지토리
 */
@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    /**
     * 사용자의 포인트 거래 내역 조회 (페이징)
     */
    Page<PointTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 사용자의 포인트 거래 내역 조회 (최근 순)
     */
    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 사용자의 특정 타입 거래 내역 조회
     */
    List<PointTransaction> findByUserAndTransactionTypeOrderByCreatedAtDesc(User user, PointTransaction.TransactionType transactionType);

    /**
     * 사용자의 특정 소스 거래 내역 조회
     */
    List<PointTransaction> findByUserAndSourceOrderByCreatedAtDesc(User user, PointTransaction.PointSource source);

    /**
     * 특정 기간의 포인트 거래 내역 조회
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user AND " +
           "pt.createdAt >= :startDate AND pt.createdAt <= :endDate " +
           "ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByUserInPeriod(@Param("user") User user, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 총 획득 포인트 계산
     */
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'EARN'")
    Long getTotalEarnedPoints(@Param("user") User user);

    /**
     * 사용자의 총 사용 포인트 계산
     */
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'SPEND'")
    Long getTotalSpentPoints(@Param("user") User user);

    /**
     * 특정 참조 ID로 거래 내역 조회 (챌린지 ID 등)
     */
    List<PointTransaction> findByUserAndReferenceIdOrderByCreatedAtDesc(User user, Long referenceId);

    /**
     * 특정 소스와 참조 ID의 거래 내역 존재 여부 확인
     */
    boolean existsByUserAndSourceAndReferenceId(User user, PointTransaction.PointSource source, Long referenceId);
}
