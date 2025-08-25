package com.solsolhey.finance.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeEstimateRequest {
    @JsonProperty("Header")
    private Header header;
    private String currency;          // 소유 통화코드 (예: USD)
    private String exchangeCurrency;  // 환전 대상 통화코드 (예: JPY)
    private String amount;            // 환전 금액 (문자열 유지)

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header {
        private String apiName; // estimate
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String fintechAppNo;
        private String apiServiceCode; // estimate
        private String institutionTransactionUniqueNo;
        private String apiKey;
    }
}
