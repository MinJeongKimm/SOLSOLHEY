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
public class CreditRatingRequest {
    @JsonProperty("Header")
    private Header header;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header {
        private String apiName; // inquireMyCreditRating
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String fintechAppNo;
        private String apiServiceCode; // inquireMyCreditRating
        private String institutionTransactionUniqueNo;
        private String apiKey;
        private String userKey; // 필수
    }
}

