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
                .onErrorResume(error -> {
                    log.error("환율 전체 조회 실패", error);
                    ExchangeRateResponse err = ExchangeRateResponse.builder()
                            .code("error")
                            .message("환율 조회에 실패했습니다.")
                            .error(error.getMessage())
                            .build();
                    return Mono.just(ResponseEntity.internalServerError().body(err));
                });
    }

    // 환율 단건 조회
    @GetMapping("/exchange-rate")
    public Mono<ResponseEntity<SingleExchangeRateResponse>> getExchangeRate(@RequestParam("currency") String currency) {
        log.info("환율 단건 조회 API 호출: {}", currency);
        return financeService.getExchangeRate(currency)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("환율 단건 조회 실패", error);
                    SingleExchangeRateResponse err = SingleExchangeRateResponse.builder()
                            .code("error")
                            .message("환율 단건 조회에 실패했습니다.")
                            .currencyCode(currency)
                            .build();
                    return Mono.just(ResponseEntity.internalServerError().body(err));
                });
    }

    // 환전 예상 금액 조회
    @PostMapping("/exchange/estimate")
    public Mono<ResponseEntity<ExchangeEstimateResponse>> estimateExchange(@RequestBody EstimateRequest request) {
        log.info("환전 예상 금액 조회 API 호출: {} -> {}", request.getCurrency(), request.getExchangeCurrency());
        return financeService.estimateExchange(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("환전 예상 금액 조회 실패", error);
                    ExchangeEstimateResponse err = ExchangeEstimateResponse.builder()
                            .code("error")
                            .message("환전 예상 금액 조회에 실패했습니다.")
                            .build();
                    return Mono.just(ResponseEntity.internalServerError().body(err));
                });
    }

    // 계좌 거래내역 조회
    @PostMapping("/accounts/transactions")
    public Mono<ResponseEntity<TransactionsResponse>> getTransactionHistory(@RequestBody TransactionHistoryRequest request) {
        log.info("계좌 거래내역 조회 API 호출: {}", request.getAccountNo());
        return financeService.getTransactionHistory(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("계좌 거래내역 조회 실패", error);
                    TransactionsResponse err = TransactionsResponse.builder()
                            .code("error")
                            .message("계좌 거래내역 조회에 실패했습니다.")
                            .build();
                    return Mono.just(ResponseEntity.internalServerError().body(err));
                });
    }
}
