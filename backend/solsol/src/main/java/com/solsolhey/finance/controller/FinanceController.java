package com.solsolhey.finance.controller;

import com.solsolhey.finance.dto.request.EstimateRequest;
import com.solsolhey.finance.dto.response.ExchangeEstimateResponse;
import com.solsolhey.finance.dto.response.ExchangeRateResponse;
import com.solsolhey.finance.dto.response.CreditRatingResponse;
import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.finance.dto.response.SingleExchangeRateResponse;
import com.solsolhey.finance.service.FinanceService;
import com.solsolhey.finance.service.FinanceUserProvisioningService;
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
    private final FinanceUserProvisioningService financeUserProvisioningService;

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


    // 신용등급 조회 (현재 로그인 사용자 기반)
    @GetMapping("/credit-rating")
    public Mono<ResponseEntity<CreditRatingResponse>> getMyCreditRating(@org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("신용등급 조회 API 호출 (current user)");
        String userKey = userDetails != null ? userDetails.getUser().getFinanceUserKey() : null;
        if (userKey == null || userKey.isBlank()) {
            // 동기 프로비저닝 시도 후 진행
            Long userId = userDetails != null ? userDetails.getUserId() : null;
            String ensured = (userId != null) ? financeUserProvisioningService.provisionAndGetUserKey(userId) : null;
            if (ensured == null || ensured.isBlank()) {
                CreditRatingResponse err = CreditRatingResponse.builder()
                        .code("error")
                        .message("금융 사용자 키 발급 중입니다. 잠시 후 다시 시도해 주세요.")
                        .build();
                return Mono.just(ResponseEntity.status(202).body(err));
            }
            userKey = ensured;
        }
        return financeService.getMyCreditRating(userKey)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("신용등급 조회 실패", error);
                    CreditRatingResponse err = CreditRatingResponse.builder()
                            .code("error")
                            .message("신용등급 조회에 실패했습니다.")
                            .build();
                    return Mono.just(ResponseEntity.internalServerError().body(err));
                });
    }
}
