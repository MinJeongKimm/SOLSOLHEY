package com.solsolhey.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimateRequest {
    private String currency;          // 소유 통화 (예: USD)
    private String exchangeCurrency;  // 대상 통화 (예: JPY)
    private String amount;            // 환전 금액 (문자열/정수 가능, 외부엔 문자열로 전달)
}

