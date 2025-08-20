package com.solsolhey.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeEstimateResponse {
    
    private String code;
    private EstimateData payload;
    private String message;
    private String error;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstimateData {
        private String baseCurrency;
        private String targetCurrency;
        private BigDecimal exchangeRate;
        private BigDecimal originalAmount;
        private BigDecimal estimatedAmount;
        private String calculatedAt;
    }
}
