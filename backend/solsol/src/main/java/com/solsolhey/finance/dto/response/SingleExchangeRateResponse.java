package com.solsolhey.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleExchangeRateResponse {
    private String code; // success / error
    private String message;

    // 최소 필드
    private String currencyCode; // 예: USD
    private String exchangeRate; // 문자열 그대로 유지
}

