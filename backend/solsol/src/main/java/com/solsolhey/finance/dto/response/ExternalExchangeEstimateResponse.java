package com.solsolhey.finance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalExchangeEstimateResponse {
    @JsonProperty("Header")
    private Header header;
    @JsonProperty("REC")
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
