package com.solsolhey.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "환전 예상 금액 조회 요청")
public class ExchangeEstimateRequest {
    
    @NotBlank(message = "기준 통화는 필수입니다")
    @Schema(description = "기준 통화 코드", example = "USD")
    private String baseCurrency;
    
    @NotBlank(message = "대상 통화는 필수입니다")
    @Schema(description = "대상 통화 코드", example = "KRW")
    private String targetCurrency;
    
    @NotNull(message = "환전 금액은 필수입니다")
    @Positive(message = "환전 금액은 양수여야 합니다")
    @Schema(description = "환전할 금액", example = "100.00")
    private BigDecimal amount;
}
