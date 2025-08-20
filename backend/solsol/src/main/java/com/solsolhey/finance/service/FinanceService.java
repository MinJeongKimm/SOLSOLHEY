package com.solsolhey.finance.service;

import com.solsolhey.finance.dto.ExchangeEstimateRequest;
import com.solsolhey.finance.dto.ExchangeEstimateResponse;
import com.solsolhey.finance.dto.ExchangeRateResponse;
import com.solsolhey.finance.dto.TransactionHistoryRequest;
import com.solsolhey.finance.dto.TransactionHistoryResponse;
import reactor.core.publisher.Mono;

public interface FinanceService {
    
    /**
     * 환율 전체 조회
     * @param base 기준 통화 (선택)
     * @return 환율 전체 목록
     */
    Mono<ExchangeRateResponse> getAllExchangeRates(String base);
    
    /**
     * 환율 단건 조회
     * @param base 기준 통화
     * @return 특정 환율 정보
     */
    Mono<ExchangeRateResponse> getExchangeRate(String base);
    
    /**
     * 환전 예상 금액 조회
     * @param request 환전 예상 금액 조회 요청
     * @return 환전 예상 금액 정보
     */
    Mono<ExchangeEstimateResponse> getExchangeEstimate(ExchangeEstimateRequest request);
    
    /**
     * 계좌 거래내역 조회
     * @param request 계좌 거래내역 조회 요청
     * @return 계좌 거래내역 목록
     */
    Mono<TransactionHistoryResponse> getTransactionHistory(TransactionHistoryRequest request);
}
