package com.solsolhey.finance.client;

import com.solsolhey.finance.config.FinanceApiProperties;
import com.solsolhey.finance.dto.request.ExchangeEstimateRequest;
import com.solsolhey.finance.dto.request.ExchangeRateRequest;
import com.solsolhey.finance.dto.request.ExchangeRateSearchRequest;
import com.solsolhey.finance.dto.request.TransactionHistoryListRequest;
import com.solsolhey.finance.dto.response.ExternalExchangeEstimateResponse;
import com.solsolhey.finance.dto.response.ExternalExchangeRateResponse;
import com.solsolhey.finance.dto.response.ExternalExchangeRateSearchResponse;
import com.solsolhey.finance.dto.response.ExternalTransactionHistoryResponse;
import com.solsolhey.finance.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceApiClient {

    @Qualifier("financeWebClient")
    private final WebClient webClient;
    private final FinanceApiProperties properties;

    public Mono<ExternalExchangeRateResponse> getExchangeRates() {
        String url = "/exchangeRate";
        ExchangeRateRequest request = buildExchangeRateRequest();
        log.info("환율 전체 조회 API 호출: {}", url);
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ExternalExchangeRateResponse.class)
                .doOnSuccess(response -> log.info("환율 전체 조회 API 호출 성공"))
                .doOnError(error -> log.error("환율 전체 조회 API 호출 실패: {}", error.getMessage()))
                .onErrorMap(error -> new ExternalApiException("환율 정보 조회에 실패했습니다", error));
    }

    public Mono<ExternalExchangeRateSearchResponse> getExchangeRateByCurrency(String currency) {
        String url = "/exchangeRate/exchangeRateSearch";
        ExchangeRateSearchRequest request = buildExchangeRateSearchRequest(currency);
        log.info("환율 단건 조회 API 호출: {} ({})", url, currency);
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ExternalExchangeRateSearchResponse.class)
                .doOnSuccess(response -> log.info("환율 단건 조회 API 호출 성공"))
                .doOnError(error -> log.error("환율 단건 조회 API 호출 실패: {}", error.getMessage()))
                .onErrorMap(error -> new ExternalApiException("환율 단건 조회에 실패했습니다", error));
    }

    public Mono<ExternalExchangeEstimateResponse> estimateExchange(String currency, String exchangeCurrency, String amount) {
        String url = "/exchange/estimate";
        ExchangeEstimateRequest request = buildEstimateRequest(currency, exchangeCurrency, amount);
        log.info("환전 예상 금액 조회 API 호출: {} ({}->{}, amount={})", url, currency, exchangeCurrency, amount);
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ExternalExchangeEstimateResponse.class)
                .doOnSuccess(response -> log.info("환전 예상 금액 조회 API 호출 성공"))
                .doOnError(error -> log.error("환전 예상 금액 조회 API 호출 실패: {}", error.getMessage()))
                .onErrorMap(error -> new ExternalApiException("환전 예상 금액 조회에 실패했습니다", error));
    }

    public Mono<ExternalTransactionHistoryResponse> getTransactionHistory(String userKey, String accountNo, String startDate, String endDate, String transactionType, String orderByType) {
        String url = "/demandDeposit/inquireTransactionHistoryList";
        TransactionHistoryListRequest request = buildTransactionHistoryListRequest(userKey, accountNo, startDate, endDate, transactionType, orderByType);
        log.info("계좌거래내역 조회 API 호출: {} ({})", url, accountNo);
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ExternalTransactionHistoryResponse.class)
                .doOnSuccess(response -> log.info("계좌거래내역 조회 API 호출 성공"))
                .doOnError(error -> log.error("계좌거래내역 조회 API 호출 실패: {}", error.getMessage()))
                .onErrorMap(error -> new ExternalApiException("계좌거래내역 조회에 실패했습니다", error));
    }

    private ExchangeRateRequest buildExchangeRateRequest() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = currentDate + currentTime + String.format("%06d", (int) (Math.random() * 1000000));

        ExchangeRateRequest.Header header = ExchangeRateRequest.Header.builder()
                .apiName("exchangeRate")
                .transmissionDate(currentDate)
                .transmissionTime(currentTime)
                .institutionCode(properties.getInstitutionCode())
                .fintechAppNo(properties.getFintechAppNo())
                .apiServiceCode("exchangeRate")
                .institutionTransactionUniqueNo(transactionId)
                .apiKey(properties.getApiKey())
                .build();

        return ExchangeRateRequest.builder()
                .header(header)
                .build();
    }

    private ExchangeRateSearchRequest buildExchangeRateSearchRequest(String currency) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = currentDate + currentTime + String.format("%06d", (int) (Math.random() * 1000000));

        ExchangeRateSearchRequest.Header header = ExchangeRateSearchRequest.Header.builder()
                .apiName("exchangeRateSearch")
                .transmissionDate(currentDate)
                .transmissionTime(currentTime)
                .institutionCode(properties.getInstitutionCode())
                .fintechAppNo(properties.getFintechAppNo())
                .apiServiceCode("exchangeRateSearch")
                .institutionTransactionUniqueNo(transactionId)
                .apiKey(properties.getApiKey())
                .build();

        return ExchangeRateSearchRequest.builder()
                .header(header)
                .currency(currency)
                .build();
    }

    private ExchangeEstimateRequest buildEstimateRequest(String currency, String exchangeCurrency, String amount) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = currentDate + currentTime + String.format("%06d", (int) (Math.random() * 1000000));

        ExchangeEstimateRequest.Header header = ExchangeEstimateRequest.Header.builder()
                .apiName("estimate")
                .transmissionDate(currentDate)
                .transmissionTime(currentTime)
                .institutionCode(properties.getInstitutionCode())
                .fintechAppNo(properties.getFintechAppNo())
                .apiServiceCode("estimate")
                .institutionTransactionUniqueNo(transactionId)
                .apiKey(properties.getApiKey())
                .build();

        return ExchangeEstimateRequest.builder()
                .header(header)
                .currency(currency)
                .exchangeCurrency(exchangeCurrency)
                .amount(amount)
                .build();
    }

    private TransactionHistoryListRequest buildTransactionHistoryListRequest(String userKey, String accountNo, String startDate, String endDate, String transactionType, String orderByType) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = currentDate + currentTime + String.format("%06d", (int) (Math.random() * 1000000));

        TransactionHistoryListRequest.Header header = TransactionHistoryListRequest.Header.builder()
                .apiName("inquireTransactionHistoryList")
                .transmissionDate(currentDate)
                .transmissionTime(currentTime)
                .institutionCode(properties.getInstitutionCode())
                .fintechAppNo(properties.getFintechAppNo())
                .apiServiceCode("inquireTransactionHistoryList")
                .institutionTransactionUniqueNo(transactionId)
                .apiKey(properties.getApiKey())
                .userKey(userKey)
                .build();

        return TransactionHistoryListRequest.builder()
                .header(header)
                .accountNo(accountNo)
                .startDate(startDate)
                .endDate(endDate)
                .transactionType(transactionType)
                .orderByType(orderByType)
                .build();
    }
}
