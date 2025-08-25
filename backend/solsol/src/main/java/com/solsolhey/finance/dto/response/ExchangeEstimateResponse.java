package com.solsolhey.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeEstimateResponse {
    private String code; // success / error
    private String message;

    // 최소 필드만 노출
    private String sourceCurrency;   // 예: USD
    private String targetCurrency;   // 예: JPY
    private String estimatedAmount;  // 대상 통화로 환전 예상 금액 (문자열 그대로 유지)
}
