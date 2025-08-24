package com.solsolhey.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateItem {
    
    private Long id;
    private String currencyCode;
    private String currencyName;
    private BigDecimal exchangeRate;
    private BigDecimal exchangeMin;
    private String created;
    
    public static ExchangeRateItem fromExternal(ExternalExchangeRateResponse.ExchangeRate external) {
        return ExchangeRateItem.builder()
                .id(external.getId())
                .currencyCode(external.getCurrency())
                .currencyName(getCurrencyName(external.getCurrency()))
                .exchangeRate(new BigDecimal(external.getExchangeRate().replace(",", "")))
                .exchangeMin(new BigDecimal(external.getExchangeMin().replace(",", "")))
                .created(external.getCreated())
                .build();
    }
    
    private static String getCurrencyName(String currencyCode) {
        return switch (currencyCode) {
            case "USD" -> "달러";
            case "EUR" -> "유로";
            case "JPY" -> "엔화";
            case "CNY" -> "위안";
            case "GBP" -> "파운드";
            case "CHF" -> "프랑";
            case "CAD" -> "캐나다 달러";
            default -> currencyCode;
        };
    }
}
