package com.solsolhey.finance.client;

import com.solsolhey.finance.config.FinanceApiProperties;
import com.solsolhey.finance.dto.request.ExchangeRateRequest;
import com.solsolhey.finance.dto.response.ExternalExchangeRateResponse;
import com.solsolhey.finance.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceApiClient {
    
    @Qualifier("financeWebClient")
    private final WebClient webClient;
    private final FinanceApiProperties properties;
    
    public Mono<ExternalExchangeRateResponse> getExchangeRates() {
        String url = "/exchangeRate";
        
        ExchangeRateRequest request = buildExchangeRateRequest();
        
        log.info("환율 전체 조회 API 호출: {}", url);
        
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ExternalExchangeRateResponse.class)
                .doOnSuccess(response -> log.info("환율 전체 조회 API 호출 성공"))
                .doOnError(error -> log.error("환율 전체 조회 API 호출 실패: {}", error.getMessage()))
                .onErrorMap(error -> new ExternalApiException("환율 정보 조회에 실패했습니다", error));
    }
    
    private ExchangeRateRequest buildExchangeRateRequest() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = currentDate + currentTime + String.format("%06d", (int)(Math.random() * 1000000));
        
        ExchangeRateRequest.Header header = ExchangeRateRequest.Header.builder()
                .apiName("exchangeRate")
                .transmissionDate(currentDate)
                .transmissionTime(currentTime)
                .institutionCode(properties.getInstitutionCode())
                .fintechAppNo(properties.getFintechAppNo())
                .apiServiceCode("exchangeRate")
                .institutionTransactionUniqueNo(transactionId)
                .apiKey(properties.getApiKey())
                .build();
        
        return ExchangeRateRequest.builder()
                .header(header)
                .build();
    }
}
