package com.solsolhey.finance.service;

import com.solsolhey.finance.client.FinanceApiClient;
import com.solsolhey.finance.dto.request.EstimateRequest;
import com.solsolhey.finance.dto.response.ExchangeRateResponse;
import com.solsolhey.finance.dto.response.ExternalExchangeRateResponse;
import com.solsolhey.finance.dto.response.ExchangeEstimateResponse;
import com.solsolhey.finance.exception.ExternalApiException;
import com.solsolhey.finance.dto.response.SingleExchangeRateResponse;
import com.solsolhey.finance.dto.response.ExternalCreditRatingResponse;
import com.solsolhey.finance.dto.response.CreditRatingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Finance 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinanceServiceImpl implements FinanceService {
    
    private final FinanceApiClient financeApiClient;
    
    @Override
    public Mono<ExchangeRateResponse> getAllExchangeRates() {
        log.info("환율 전체 조회 시작");
        
        return financeApiClient.getExchangeRates()
                .map(this::convertToExchangeRateResponse)
                .doOnSuccess(response -> log.info("환율 전체 조회 완료: {} 개 통화", 
                        response.getPayload() != null ? response.getPayload().size() : 0))
                .doOnError(error -> log.error("환율 전체 조회 중 오류 발생", error))
                .onErrorResume(error -> {
                    log.error("환율 전체 조회 실패", error);
                    if (error instanceof ExternalApiException) {
                        return Mono.error(error);
                    }
                    return Mono.error(new ExternalApiException("환율 정보를 조회할 수 없습니다. 잠시 후 다시 시도해 주세요."));
                });
    }

    @Override
    public Mono<SingleExchangeRateResponse> getExchangeRate(String currency) {
        log.info("환율 단건 조회 시작: {}", currency);
        return financeApiClient.getExchangeRates()
                .map(external -> {
                    if (external == null || external.getRec() == null) {
                        return SingleExchangeRateResponse.builder()
                                .code("error")
                                .message("환율 정보를 가져올 수 없습니다.")
                                .currencyCode(currency)
                                .build();
                    }
                    var match = external.getRec().stream()
                            .filter(r -> currency.equalsIgnoreCase(r.getCurrency()))
                            .findFirst();
                    return match.map(r -> SingleExchangeRateResponse.builder()
                                    .code("success")
                                    .message("환율 단건 조회 완료")
                                    .currencyCode(r.getCurrency())
                                    .exchangeRate(r.getExchangeRate())
                                    .build()
                            )
                            .orElseGet(() -> SingleExchangeRateResponse.builder()
                                    .code("error")
                                    .message("요청한 통화를 찾을 수 없습니다.")
                                    .currencyCode(currency)
                                    .build());
                })
                .doOnError(error -> log.error("환율 단건 조회 중 오류 발생", error))
                .onErrorResume(error -> Mono.just(
                        SingleExchangeRateResponse.builder()
                                .code("error")
                                .message("환율 단건 조회에 실패했습니다.")
                                .currencyCode(currency)
                                .exchangeRate(null)
                                .build()
                ));
    }

    @Override
    public Mono<ExchangeEstimateResponse> estimateExchange(EstimateRequest request) {
        log.info("환전 예상 금액 조회 시작: {} -> {}, {}", request.getCurrency(), request.getExchangeCurrency(), request.getAmount());
        return financeApiClient.estimateExchange(request.getCurrency(), request.getExchangeCurrency(), String.valueOf(request.getAmount()))
                .map(external -> {
                    var rec = external.getRec();
                    var src = rec.getCurrency();
                    var dst = rec.getExchangeCurrency();
                    return ExchangeEstimateResponse.builder()
                            .code("success")
                            .message("환전 예상 금액 조회 완료")
                            .sourceCurrency(src.getCurrency())
                            .sourceAmount(src.getAmount())
                            .sourceCurrencyName(src.getCurrencyName())
                            .targetCurrency(dst.getCurrency())
                            .targetAmount(dst.getAmount())
                            .targetCurrencyName(dst.getCurrencyName())
                            .estimatedAmount(dst.getAmount()) // 하위호환 유지
                            .build();
                })
                .onErrorResume(error -> Mono.just(
                        ExchangeEstimateResponse.builder()
                                .code("error")
                                .message("환전 예상 금액 조회에 실패했습니다.")
                                .build()
                ));
    }


    @Override
    public Mono<CreditRatingResponse> getMyCreditRating(String userKey) {
        log.info("신용등급 조회 시작");
        return financeApiClient.getMyCreditRating(userKey)
                .map(this::convertToCreditRatingResponse)
                .onErrorResume(error -> Mono.just(
                        CreditRatingResponse.builder()
                                .code("error")
                                .message("신용등급 조회에 실패했습니다.")
                                .build()
                ));
    }
    
    private ExchangeRateResponse convertToExchangeRateResponse(ExternalExchangeRateResponse externalResponse) {
        if (externalResponse == null || externalResponse.getRec() == null) {
            return ExchangeRateResponse.builder()
                    .code("error")
                    .message("환율 정보를 가져올 수 없습니다.")
                    .build();
        }
        
        List<com.solsolhey.finance.dto.response.ExchangeRateItem> exchangeRates = externalResponse.getRec().stream()
                .map(com.solsolhey.finance.dto.response.ExchangeRateItem::fromExternal)
                .collect(Collectors.toList());
        
        return ExchangeRateResponse.builder()
                .code("success")
                .payload(exchangeRates)
                .message("환율 조회가 완료되었습니다.")
                .build();
    }

    // 단건 변환 도우미는 리스트 필터 방식으로 대체됨

    

    private CreditRatingResponse convertToCreditRatingResponse(ExternalCreditRatingResponse external) {
        if (external == null || external.getRec() == null) {
            return CreditRatingResponse.builder()
                    .code("error")
                    .message("신용등급 정보를 가져올 수 없습니다.")
                    .build();
        }
        var rec = external.getRec();
        return CreditRatingResponse.builder()
                .code("success")
                .message("신용등급 조회 완료")
                .ratingName(rec.getRatingName())
                .demandDepositAssetValue(rec.getDemandDepositAssetValue())
                .depositSavingsAssetValue(rec.getDepositSavingsAssetValue())
                .totalAssetValue(rec.getTotalAssetValue())
                .build();
    }
}
