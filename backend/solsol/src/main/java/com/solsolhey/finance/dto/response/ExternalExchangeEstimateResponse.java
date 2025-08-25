package com.solsolhey.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalExchangeEstimateResponse {
    private Header header;
    private Rec rec;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header {
        private String responseCode;
        private String responseMessage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Rec {
        private Currency currency;             // 원 통화
        private Currency exchangeCurrency;     // 대상 통화
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Currency {
        private String amount;
        private String currency;
        private String currencyName;
    }
}

