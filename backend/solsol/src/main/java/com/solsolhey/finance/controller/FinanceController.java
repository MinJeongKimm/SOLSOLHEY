package com.solsolhey.finance.controller;

import com.solsolhey.finance.dto.request.EstimateRequest;
import com.solsolhey.finance.dto.request.TransactionHistoryRequest;
import com.solsolhey.finance.dto.response.ExchangeEstimateResponse;
import com.solsolhey.finance.dto.response.ExchangeRateResponse;
import com.solsolhey.finance.dto.response.SingleExchangeRateResponse;
import com.solsolhey.finance.dto.response.TransactionsResponse;
import com.solsolhey.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@Slf4j
public class FinanceController {

    private final FinanceService financeService;

    // 환율 전체 조회
    @GetMapping("/exchange-rates")
    public Mono<ResponseEntity<ExchangeRateResponse>> getAllExchangeRates() {
        log.info("환율 전체 조회 API 호출");
        return financeService.getAllExchangeRates()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    // 환율 단건 조회
    @GetMapping("/exchange-rate")
    public Mono<ResponseEntity<SingleExchangeRateResponse>> getExchangeRate(@RequestParam("currency") String currency) {
        log.info("환율 단건 조회 API 호출: {}", currency);
        return financeService.getExchangeRate(currency)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    // 환전 예상 금액 조회
    @PostMapping("/exchange/estimate")
    public Mono<ResponseEntity<ExchangeEstimateResponse>> estimateExchange(@RequestBody EstimateRequest request) {
        log.info("환전 예상 금액 조회 API 호출: {} -> {}", request.getCurrency(), request.getExchangeCurrency());
        return financeService.estimateExchange(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    // 계좌 거래내역 조회
    @PostMapping("/accounts/transactions")
    public Mono<ResponseEntity<TransactionsResponse>> getTransactionHistory(@RequestBody TransactionHistoryRequest request) {
        log.info("계좌 거래내역 조회 API 호출: {}", request.getAccountNo());
        return financeService.getTransactionHistory(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.internalServerError().build()));
    }
}
