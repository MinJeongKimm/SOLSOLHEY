package com.solsolhey.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryListRequest {
    private Header header;
    private String accountNo;
    private String startDate;       // YYYYMMDD
    private String endDate;         // YYYYMMDD
    private String transactionType; // M/D/A
    private String orderByType;     // ASC/DESC (옵션)

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header {
        private String apiName; // inquireTransactionHistoryList
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String fintechAppNo;
        private String apiServiceCode; // inquireTransactionHistoryList
        private String institutionTransactionUniqueNo;
        private String apiKey;
        private String userKey; // 이 API는 userKey 필요
    }
}

