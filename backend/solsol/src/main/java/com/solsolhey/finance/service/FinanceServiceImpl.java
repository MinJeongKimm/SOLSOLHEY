package com.solsolhey.finance.service;

import com.solsolhey.finance.dto.ExchangeEstimateRequest;
import com.solsolhey.finance.dto.ExchangeEstimateResponse;
import com.solsolhey.finance.dto.ExchangeRateResponse;
import com.solsolhey.finance.dto.TransactionHistoryRequest;
import com.solsolhey.finance.dto.TransactionHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceServiceImpl implements FinanceService {
    
    private final FinanceApiClient financeApiClient;
    
    @Override
    public Mono<ExchangeRateResponse> getAllExchangeRates(String base) {
        log.info("환율 전체 조회 서비스 호출 - base: {}", base);
        
        return financeApiClient.getAllExchangeRates(base)
                .doOnSuccess(response -> {
                    if ("success".equals(response.getCode())) {
                        log.info("환율 전체 조회 성공 - 조회된 환율 개수: {}", 
                                response.getPayload() != null ? response.getPayload().size() : 0);
                    } else {
                        log.warn("환율 전체 조회 응답 코드: {}, 메시지: {}", response.getCode(), response.getMessage());
                    }
                })
                .doOnError(error -> log.error("환율 전체 조회 서비스 오류", error));
    }
    
    @Override
    public Mono<ExchangeRateResponse> getExchangeRate(String base) {
        log.info("환율 단건 조회 서비스 호출 - base: {}", base);
        
        if (base == null || base.trim().isEmpty()) {
            log.error("환율 단건 조회 - 기준 통화가 필요합니다");
            return Mono.error(new IllegalArgumentException("기준 통화는 필수입니다"));
        }
        
        return financeApiClient.getExchangeRate(base)
                .doOnSuccess(response -> {
                    if ("success".equals(response.getCode())) {
                        log.info("환율 단건 조회 성공 - 통화: {}", base);
                    } else {
                        log.warn("환율 단건 조회 응답 코드: {}, 메시지: {}", response.getCode(), response.getMessage());
                    }
                })
                .doOnError(error -> log.error("환율 단건 조회 서비스 오류", error));
    }
    
    @Override
    public Mono<ExchangeEstimateResponse> getExchangeEstimate(ExchangeEstimateRequest request) {
        log.info("환전 예상 금액 조회 서비스 호출 - {} {} -> {}", 
                request.getAmount(), request.getBaseCurrency(), request.getTargetCurrency());
        
        return financeApiClient.getExchangeEstimate(
                        request.getBaseCurrency(),
                        request.getTargetCurrency(),
                        request.getAmount().toString())
                .doOnSuccess(response -> {
                    if ("success".equals(response.getCode())) {
                        log.info("환전 예상 금액 조회 성공");
                    } else {
                        log.warn("환전 예상 금액 조회 응답 코드: {}, 메시지: {}", response.getCode(), response.getMessage());
                    }
                })
                .doOnError(error -> log.error("환전 예상 금액 조회 서비스 오류", error));
    }
    
    @Override
    public Mono<TransactionHistoryResponse> getTransactionHistory(TransactionHistoryRequest request) {
        log.info("계좌 거래내역 조회 서비스 호출 - 계좌: {}, 기간: {} ~ {}, 개수: {}", 
                request.getAccountNumber(), request.getStartDate(), request.getEndDate(), request.getSize());
        
        return financeApiClient.getTransactionHistory(
                        request.getAccountNumber(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getSize())
                .doOnSuccess(response -> {
                    if ("success".equals(response.getCode())) {
                        int transactionCount = 0;
                        if (response.getPayload() != null && response.getPayload().getTransactions() != null) {
                            transactionCount = response.getPayload().getTransactions().size();
                        }
                        log.info("계좌 거래내역 조회 성공 - 조회된 거래 개수: {}", transactionCount);
                    } else {
                        log.warn("계좌 거래내역 조회 응답 코드: {}, 메시지: {}", response.getCode(), response.getMessage());
                    }
                })
                .doOnError(error -> log.error("계좌 거래내역 조회 서비스 오류", error));
    }
}
