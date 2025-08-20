package com.solsolhey.finance.service;

import com.solsolhey.finance.dto.ExchangeEstimateResponse;
import com.solsolhey.finance.dto.ExchangeRateResponse;
import com.solsolhey.finance.dto.TransactionHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceApiClient {
    
    @Qualifier("financeWebClient")
    private final WebClient webClient;
    
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    /**
     * 환율 전체 조회
     */
    public Mono<ExchangeRateResponse> getAllExchangeRates(String base) {
        log.info("환율 전체 조회 API 호출 - base: {}", base);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/exchangeRate")
                        .queryParamIfPresent("base", base != null ? java.util.Optional.of(base) : java.util.Optional.empty())
                        .build())
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .timeout(TIMEOUT)
                .doOnSuccess(response -> log.info("환율 전체 조회 성공: {}", response.getCode()))
                .doOnError(error -> log.error("환율 전체 조회 실패", error))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError);
    }
    
    /**
     * 환율 단건 조회
     */
    public Mono<ExchangeRateResponse> getExchangeRate(String base) {
        log.info("환율 단건 조회 API 호출 - base: {}", base);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/exchangeRate/exchangeRateSearch")
                        .queryParamIfPresent("base", base != null ? java.util.Optional.of(base) : java.util.Optional.empty())
                        .build())
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .timeout(TIMEOUT)
                .doOnSuccess(response -> log.info("환율 단건 조회 성공: {}", response.getCode()))
                .doOnError(error -> log.error("환율 단건 조회 실패", error))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError);
    }
    
    /**
     * 환전 예상 금액 조회
     */
    public Mono<ExchangeEstimateResponse> getExchangeEstimate(String base, String target, String amount) {
        log.info("환전 예상 금액 조회 API 호출 - base: {}, target: {}, amount: {}", base, target, amount);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/exchange/estimate")
                        .queryParamIfPresent("base", base != null ? java.util.Optional.of(base) : java.util.Optional.empty())
                        .queryParamIfPresent("target", target != null ? java.util.Optional.of(target) : java.util.Optional.empty())
                        .queryParamIfPresent("amount", amount != null ? java.util.Optional.of(amount) : java.util.Optional.empty())
                        .build())
                .retrieve()
                .bodyToMono(ExchangeEstimateResponse.class)
                .timeout(TIMEOUT)
                .doOnSuccess(response -> log.info("환전 예상 금액 조회 성공: {}", response.getCode()))
                .doOnError(error -> log.error("환전 예상 금액 조회 실패", error))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError);
    }
    
    /**
     * 계좌 거래내역 조회
     */
    public Mono<TransactionHistoryResponse> getTransactionHistory(String accountNumber, String startDate, String endDate, Integer size) {
        log.info("계좌 거래내역 조회 API 호출 - accountNumber: {}, period: {} ~ {}, size: {}", 
                accountNumber, startDate, endDate, size);
        
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/demandDeposit/inquireTransactionHistoryList");
                    
                    if (accountNumber != null) builder.queryParam("accountNumber", accountNumber);
                    if (startDate != null) builder.queryParam("startDate", startDate);
                    if (endDate != null) builder.queryParam("endDate", endDate);
                    if (size != null) builder.queryParam("size", size);
                    
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(TransactionHistoryResponse.class)
                .timeout(TIMEOUT)
                .doOnSuccess(response -> log.info("계좌 거래내역 조회 성공: {}", response.getCode()))
                .doOnError(error -> log.error("계좌 거래내역 조회 실패", error))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError);
    }
    
    /**
     * WebClient 오류 처리
     */
    private <T> Mono<T> handleWebClientError(WebClientResponseException ex) {
        log.error("외부 API 호출 오류 - Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        
        // 기본 에러 응답 생성 (각 응답 타입에 맞게 조정 필요)
        return Mono.error(new RuntimeException("외부 API 호출에 실패했습니다: " + ex.getMessage()));
    }
}
