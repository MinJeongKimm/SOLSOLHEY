package com.solsolhey.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalExchangeRateResponse {
    
    private Header header;
    private List<ExchangeRate> rec;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Header {
        private String responseCode;
        private String responseMessage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExchangeRate {
        private Long id;
        private String currency;
        private String exchangeRate;
        private String exchangeMin;
        private String created;
    }
}
