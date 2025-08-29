package com.solsolhey.finance.service;

import com.solsolhey.finance.dto.request.EstimateRequest;
import com.solsolhey.finance.dto.request.TransactionHistoryRequest;
import com.solsolhey.finance.dto.response.ExchangeEstimateResponse;
import com.solsolhey.finance.dto.response.ExchangeRateResponse;
import com.solsolhey.finance.dto.response.CreditRatingResponse;
import com.solsolhey.finance.dto.response.SingleExchangeRateResponse;
import com.solsolhey.finance.dto.response.TransactionsResponse;
import reactor.core.publisher.Mono;

public interface FinanceService {

    // 환율 전체 조회
    Mono<ExchangeRateResponse> getAllExchangeRates();

    // 환율 단건 조회
    Mono<SingleExchangeRateResponse> getExchangeRate(String currency);

    // 환전 예상 금액
    Mono<ExchangeEstimateResponse> estimateExchange(EstimateRequest request);

    // 계좌 거래내역
    Mono<TransactionsResponse> getTransactionHistory(TransactionHistoryRequest request);

    // 신용등급 조회
    Mono<CreditRatingResponse> getMyCreditRating(String userKey);
}
