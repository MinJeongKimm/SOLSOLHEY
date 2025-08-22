package com.solsolhey.point.entity;

import com.solsolhey.user.entity.BaseEntity;
import com.solsolhey.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 포인트 거래 내역 엔티티
 */
@Entity
@Table(name = "point_transactions")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "reference_id")
    private Long referenceId; // 관련된 챌린지, 상품 구매 등의 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter; // 거래 후 잔액

    @Builder
    public PointTransaction(User user, Integer pointAmount, TransactionType transactionType, 
                           String description, Long referenceId, ReferenceType referenceType, Integer balanceAfter) {
        this.user = user;
        this.pointAmount = pointAmount;
        this.transactionType = transactionType;
        this.description = description;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.balanceAfter = balanceAfter;
    }

    public enum TransactionType {
        EARN,       // 적립
        SPEND,      // 사용
        REFUND,     // 환불
        BONUS       // 보너스
    }

    public enum ReferenceType {
        CHALLENGE,  // 챌린지 완료
        PURCHASE,   // 상품 구매
        DAILY_BONUS,// 출석 보너스
        EVENT,      // 이벤트
        ADMIN       // 관리자 지급
    }
}
