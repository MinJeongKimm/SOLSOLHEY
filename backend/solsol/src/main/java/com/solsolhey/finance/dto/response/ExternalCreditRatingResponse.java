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
public class ExternalCreditRatingResponse {
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
        private String apiName;
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String apiKey;
        private String apiServiceCode;
        private String institutionTransactionUniqueNo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Rec {
        private String ratingName;
        private String demandDepositAssetValue;
        private String depositSavingsAssetValue;
        private String totalAssetValue;
    }
}

