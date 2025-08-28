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
    private String sourceAmount;     // 예: 1,000
    private String sourceCurrencyName; // 예: 달러
    private String targetCurrency;   // 예: JPY
    private String targetAmount;     // 대상 통화 금액 (문자열 그대로 유지)
    private String targetCurrencyName; // 예: 엔화
    // 하위호환: 기존 필드 유지 (targetAmount와 동일하게 채움)
    private String estimatedAmount;  // 대상 통화로 환전 예상 금액 (문자열 그대로 유지)
}
