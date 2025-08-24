package com.solsolhey.finance.controller;

import com.solsolhey.finance.dto.response.ExchangeRateResponse;
import com.solsolhey.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@Slf4j
public class FinanceController {
    
    private final FinanceService financeService;
    
    @GetMapping("/exchange-rates")
    public Mono<ResponseEntity<ExchangeRateResponse>> getAllExchangeRates() {
        log.info("환율 전체 조회 API 호출");
        
        return financeService.getAllExchangeRates()
                .map(response -> ResponseEntity.ok(response))
                .doOnError(error -> log.error("환율 전체 조회 중 오류 발생", error))
                .onErrorResume(error -> {
                    log.error("환율 전체 조회 실패", error);
                    ExchangeRateResponse errorResponse = ExchangeRateResponse.builder()
                            .code("error")
                            .message("환율 조회에 실패했습니다.")
                            .error(error.getMessage())
                            .build();
                    return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
                });
    }
}
