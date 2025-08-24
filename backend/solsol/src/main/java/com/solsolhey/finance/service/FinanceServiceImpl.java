package com.solsolhey.finance.service;

import com.solsolhey.finance.client.FinanceApiClient;
import com.solsolhey.finance.dto.response.ExchangeRateResponse;
import com.solsolhey.finance.dto.response.ExternalExchangeRateResponse;
import com.solsolhey.finance.exception.ExternalApiException;
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
}
