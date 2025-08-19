package com.solsolhey.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateResponse {
    
    private String code;
    private List<ExchangeRate> payload;
    private String message;
    private String error;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExchangeRate {
        private String currencyCode;
        private String currencyName;
        private String baseRate;
        private String buyRate;
        private String sellRate;
        private String dealBasDate;
        private String createdAt;
        private String modifiedAt;
    }
}
