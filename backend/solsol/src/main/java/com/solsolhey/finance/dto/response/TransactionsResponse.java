package com.solsolhey.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionsResponse {
    private String code; // success / error
    private String message;
    private String totalCount;
    private List<TransactionItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionItem {
        private String date;    // YYYYMMDD
        private String time;    // HHmmss
        private String typeName; // 입금/출금 등
        private String amount;  // 거래금액
    }
}

