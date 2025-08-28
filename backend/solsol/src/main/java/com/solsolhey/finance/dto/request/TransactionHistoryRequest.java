package com.solsolhey.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryRequest {
    private String userKey;           // 외부 API 헤더에 필요
    private String accountNo;
    private String startDate;         // YYYYMMDD
    private String endDate;           // YYYYMMDD
    private String transactionType;   // M/D/A
    private String orderByType;       // ASC/DESC (옵션)
}

