package com.solsolhey.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateSearchRequest {
    private Header header;
    private String currency; // 조회 대상 통화코드 (예: USD)

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header {
        private String apiName; // exchangeRateSearch
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String fintechAppNo;
        private String apiServiceCode; // exchangeRateSearch
        private String institutionTransactionUniqueNo;
        private String apiKey;
    }
}

