package com.solsolhey.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditRatingResponse {
    private String code;      // success | error
    private String message;   // 메시지

    // Payload
    private String ratingName;
    private String demandDepositAssetValue;
    private String depositSavingsAssetValue;
    private String totalAssetValue;
}

