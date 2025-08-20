package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 거래 내역 엔티티
 */
@Entity
@Table(name = "point_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_transaction_id")
    private Long pointTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private PointSource source;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "reference_id")
    private Long referenceId; // 챌린지 ID, 업적 ID, 아이템 ID 등

    @Builder
    public PointTransaction(User user, TransactionType transactionType, Integer points,
                           PointSource source, String description, Long referenceId) {
        this.user = user;
        this.transactionType = transactionType;
        this.points = Math.abs(points); // 항상 양수로 저장
        this.source = source;
        this.description = description;
        this.referenceId = referenceId;
    }

    /**
     * 포인트 변화량 계산 (획득시 +, 소비시 -)
     */
    public Integer getPointChange() {
        if (points == null) return 0;
        return transactionType == TransactionType.EARN ? points : -points;
    }

    /**
     * 거래 타입 열거형
     */
    public enum TransactionType {
        EARN("획득"),
        SPEND("소비"),
        REFUND("환불");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 포인트 소스 열거형
     */
    public enum PointSource {
        CHALLENGE("챌린지 완료"),
        ACHIEVEMENT("업적 달성"),
        ATTENDANCE("출석 체크"),
        PURCHASE("상점 구매"),
        ADMIN("관리자 지급"),
        EVENT("이벤트 참여"),
        SOCIAL("소셜 활동"),
        FRIEND_INVITE("친구 초대"),
        FRIEND_INTERACTION("친구 상호작용"),
        REFUND("환불");

        private final String displayName;

        PointSource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
