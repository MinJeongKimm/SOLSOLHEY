package com.solsolhey.finance.service;

import com.solsolhey.finance.dto.response.ExchangeRateResponse;
import reactor.core.publisher.Mono;

public interface FinanceService {
    
    /**
     * 환율 전체 조회
     * @return 환율 전체 목록
     */
    Mono<ExchangeRateResponse> getAllExchangeRates();
}
