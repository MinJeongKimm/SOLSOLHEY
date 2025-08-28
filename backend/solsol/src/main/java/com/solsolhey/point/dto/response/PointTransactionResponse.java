package com.solsolhey.point.dto.response;

import com.solsolhey.point.entity.PointTransaction;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import com.solsolhey.point.entity.PointTransaction.TransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 포인트 거래 내역 응답 DTO
 */
@Builder
public record PointTransactionResponse(
    Long transactionId,
    Long userId,
    String userNickname,
    Integer pointAmount,
    TransactionType transactionType,
    String description,
    Long referenceId,
    ReferenceType referenceType,
    Integer balanceAfter,
    LocalDateTime createdAt
) {
    public static PointTransactionResponse from(PointTransaction transaction) {
        return PointTransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUser().getUserId())
                .userNickname(transaction.getUser().getNickname())
                .pointAmount(transaction.getPointAmount())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .referenceType(transaction.getReferenceType())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
