package com.solsolhey.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryResponse {
    
    private String code;
    private TransactionData payload;
    private String message;
    private String error;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionData {
        private String accountNumber;
        private String accountName;
        private BigDecimal currentBalance;
        private List<Transaction> transactions;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Transaction {
        private String transactionId;
        private String transactionType;
        private String transactionTypeName;
        private BigDecimal transactionAmount;
        private BigDecimal balanceAfterTransaction;
        private String transactionSummary;
        private String transactionDate;
        private String transactionTime;
        private String counterpartyName;
        private String counterpartyAccountNumber;
    }
}
