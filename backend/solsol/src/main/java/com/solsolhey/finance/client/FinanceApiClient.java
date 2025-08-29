package com.solsolhey.finance.client;

import com.solsolhey.finance.config.FinanceApiProperties;
import com.solsolhey.finance.dto.request.ExchangeEstimateRequest;
import com.solsolhey.finance.dto.request.ExchangeRateRequest;
import com.solsolhey.finance.dto.request.TransactionHistoryListRequest;
import com.solsolhey.finance.dto.request.CreditRatingRequest;
import com.solsolhey.finance.dto.request.MemberCreateRequest;
import com.solsolhey.finance.dto.request.MemberSearchRequest;
import com.solsolhey.finance.dto.response.ExternalExchangeEstimateResponse;
import com.solsolhey.finance.dto.response.ExternalExchangeRateResponse;
import com.solsolhey.finance.dto.response.ExternalTransactionHistoryResponse;
import com.solsolhey.finance.dto.response.ExternalCreditRatingResponse;
import com.solsolhey.finance.dto.response.MemberResponse;
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
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    String msg = String.format("외부 API 오류(%s): %s", clientResponse.statusCode().value(), body);
                                    log.error(msg);
                                    return Mono.error(new ExternalApiException("환율 정보 조회에 실패했습니다: " + msg));
                                });
                    }
                    return clientResponse.bodyToMono(ExternalExchangeRateResponse.class);
                })
                .doOnSuccess(response -> log.info("환율 전체 조회 API 호출 성공"));
    }

    /**
     * 금융 사용자 생성 (회원가입)
     */
    public Mono<MemberResponse> createMember(String financeEmail) {
        String url = properties.getMemberBaseUrl() + "/member/"; // absolute URL
        MemberCreateRequest request = new MemberCreateRequest(properties.getApiKey(), financeEmail);
        log.info("금융 회원 생성 API 호출: {} ({})", url, maskEmail(financeEmail));
        return WebClient.create()
                .post()
                .uri(url)
                .bodyValue(request)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    String msg = String.format("외부 API 오류(%s): %s", clientResponse.statusCode().value(), body);
                                    log.error(msg);
                                    return Mono.error(new ExternalApiException("금융 회원 생성 실패: " + msg));
                                });
                    }
                    return clientResponse.bodyToMono(MemberResponse.class);
                })
                .doOnSuccess(res -> log.info("금융 회원 생성 성공: userKey={} ({}).", maskKey(res.getUserKey()), maskEmail(financeEmail)));
    }

    /**
     * 금융 사용자 조회 (중복 시 userKey 회수)
     */
    public Mono<MemberResponse> searchMember(String financeEmail) {
        String url = properties.getMemberBaseUrl() + "/member/search"; // absolute URL
        MemberSearchRequest request = new MemberSearchRequest(financeEmail, properties.getApiKey());
        log.info("금융 회원 조회 API 호출: {} ({})", url, maskEmail(financeEmail));
        return WebClient.create()
                .post()
                .uri(url)
                .bodyValue(request)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    String msg = String.format("외부 API 오류(%s): %s", clientResponse.statusCode().value(), body);
                                    log.error(msg);
                                    return Mono.error(new ExternalApiException("금융 회원 조회 실패: " + msg));
                                });
                    }
                    return clientResponse.bodyToMono(MemberResponse.class);
                })
                .doOnSuccess(res -> log.info("금융 회원 조회 성공: userKey={} ({}).", maskKey(res.getUserKey()), maskEmail(financeEmail)));
    }

    public Mono<ExternalExchangeEstimateResponse> estimateExchange(String currency, String exchangeCurrency, String amount) {
        String url = "/exchange/estimate";
        ExchangeEstimateRequest request = buildEstimateRequest(currency, exchangeCurrency, amount);
        log.info("환전 예상 금액 조회 API 호출: {} ({}->{}, amount={})", url, currency, exchangeCurrency, amount);
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    String msg = String.format("외부 API 오류(%s): %s", clientResponse.statusCode().value(), body);
                                    log.error(msg);
                                    return Mono.error(new ExternalApiException("환전 예상 금액 조회에 실패했습니다: " + msg));
                                });
                    }
                    return clientResponse.bodyToMono(ExternalExchangeEstimateResponse.class);
                })
                .doOnSuccess(response -> log.info("환전 예상 금액 조회 API 호출 성공"));
    }

    public Mono<ExternalTransactionHistoryResponse> getTransactionHistory(String userKey, String accountNo, String startDate, String endDate, String transactionType, String orderByType) {
        String url = "/demandDeposit/inquireTransactionHistoryList";
        TransactionHistoryListRequest request = buildTransactionHistoryListRequest(userKey, accountNo, startDate, endDate, transactionType, orderByType);
        // Debug 로그: 민감정보 마스킹 후 전송 값 확인
        TransactionHistoryListRequest.Header h = request.getHeader();
        log.info("외부 거래내역 요청 Header: apiName={}, transmissionDate={}, transmissionTime={}, institutionCode={}, fintechAppNo={}, apiServiceCode={}, uniqueNo={}, userKey={}",
                h.getApiName(), h.getTransmissionDate(), h.getTransmissionTime(), h.getInstitutionCode(), h.getFintechAppNo(), h.getApiServiceCode(), h.getInstitutionTransactionUniqueNo(), maskKey(h.getUserKey()));
        log.info("외부 거래내역 요청 Body: accountNo={}, startDate={}, endDate={}, transactionType={}, orderByType={}",
                request.getAccountNo(), request.getStartDate(), request.getEndDate(), request.getTransactionType(), request.getOrderByType());
        log.info("계좌거래내역 조회 API 호출: {} ({})", url, accountNo);
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    String msg = String.format("외부 API 오류(%s): %s", clientResponse.statusCode().value(), body);
                                    log.error(msg);
                                    return Mono.error(new ExternalApiException("계좌거래내역 조회에 실패했습니다: " + msg));
                                });
                    }
                    return clientResponse.bodyToMono(ExternalTransactionHistoryResponse.class);
                })
                .doOnSuccess(response -> log.info("계좌거래내역 조회 API 호출 성공"));
    }

    public Mono<ExternalCreditRatingResponse> getMyCreditRating(String userKey) {
        String url = "/loan/inquireMyCreditRating";
        CreditRatingRequest request = buildCreditRatingRequest(userKey);
        CreditRatingRequest.Header h = request.getHeader();
        log.info("신용등급 조회 요청 Header: apiName={}, transmissionDate={}, transmissionTime={}, institutionCode={}, fintechAppNo={}, apiServiceCode={}, uniqueNo={}, userKey={}",
                h.getApiName(), h.getTransmissionDate(), h.getTransmissionTime(), h.getInstitutionCode(), h.getFintechAppNo(), h.getApiServiceCode(), h.getInstitutionTransactionUniqueNo(), maskKey(h.getUserKey()));
        log.info("신용등급 조회 API 호출: {} (userKey={})", url, maskKey(userKey));
        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    String msg = String.format("외부 API 오류(%s): %s", clientResponse.statusCode().value(), body);
                                    log.error(msg);
                                    return Mono.error(new ExternalApiException("신용등급 조회에 실패했습니다: " + msg));
                                });
                    }
                    return clientResponse.bodyToMono(ExternalCreditRatingResponse.class);
                })
                .doOnSuccess(response -> log.info("신용등급 조회 API 호출 성공"));
    }

    private ExchangeRateRequest buildExchangeRateRequest() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String currentDate = nowKst.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = nowKst.format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = generateInstitutionTransactionId(nowKst);

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


    private ExchangeEstimateRequest buildEstimateRequest(String currency, String exchangeCurrency, String amount) {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String currentDate = nowKst.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = nowKst.format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = generateInstitutionTransactionId(nowKst);

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
        // 입력 정규화: 공백 제거, 대문자화 등
        String safeUserKey = trim(userKey);
        String safeAccountNo = trim(accountNo);
        String safeStartDate = trim(startDate);
        String safeEndDate = trim(endDate);
        String safeTransactionType = upper(trim(transactionType));
        String safeOrderByType = upper(trim(orderByType));

        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String currentDate = nowKst.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = nowKst.format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = generateInstitutionTransactionId(nowKst);

        TransactionHistoryListRequest.Header header = TransactionHistoryListRequest.Header.builder()
                .apiName("inquireTransactionHistoryList")
                .transmissionDate(currentDate)
                .transmissionTime(currentTime)
                .institutionCode(properties.getInstitutionCode())
                .fintechAppNo(properties.getFintechAppNo())
                .apiServiceCode("inquireTransactionHistoryList")
                .institutionTransactionUniqueNo(transactionId)
                .apiKey(properties.getApiKey())
                .userKey(safeUserKey)
                .build();

        return TransactionHistoryListRequest.builder()
                .header(header)
                .accountNo(safeAccountNo)
                .startDate(safeStartDate)
                .endDate(safeEndDate)
                .transactionType(safeTransactionType)
                .orderByType(safeOrderByType)
                .build();
    }

    private CreditRatingRequest buildCreditRatingRequest(String userKey) {
        String safeUserKey = trim(userKey);

        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String currentDate = nowKst.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentTime = nowKst.format(DateTimeFormatter.ofPattern("HHmmss"));
        String transactionId = generateInstitutionTransactionId(nowKst);

        CreditRatingRequest.Header header = CreditRatingRequest.Header.builder()
                .apiName("inquireMyCreditRating")
                .transmissionDate(currentDate)
                .transmissionTime(currentTime)
                .institutionCode(properties.getInstitutionCode())
                .fintechAppNo(properties.getFintechAppNo())
                .apiServiceCode("inquireMyCreditRating")
                .institutionTransactionUniqueNo(transactionId)
                .apiKey(properties.getApiKey())
                .userKey(safeUserKey)
                .build();

        return CreditRatingRequest.builder()
                .header(header)
                .build();
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
    private String upper(String s) { return s == null ? null : s.toUpperCase(); }
    private String maskKey(String key) {
        if (key == null || key.isEmpty()) return "null";
        int n = key.length();
        if (n <= 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(n - 4);
    }

    private String maskEmail(String email) {
        if (email == null) return "(null)";
        int at = email.indexOf('@');
        if (at <= 1) return "*" + email.substring(Math.max(0, at));
        return email.charAt(0) + "****" + email.substring(at);
    }

    /**
     * 기관거래고유번호 생성: 정확히 20자리 (yyyyMMddHHmmss + 6자리 난수)
     * - 시간 기준은 Asia/Seoul
     * - 난수는 SecureRandom 사용
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private String generateInstitutionTransactionId(ZonedDateTime nowKst) {
        String dateTime14 = nowKst.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = SECURE_RANDOM.nextInt(1_000_000); // 0..999999
        String tail6 = String.format("%06d", rand);
        String id = dateTime14 + tail6; // 20 chars
        if (id.length() != 20) {
            // 방어적 보정: 길이가 다를 경우 맞춰서 패딩/절단
            id = (dateTime14 + String.format("%06d", rand)).substring(0, Math.min(20, dateTime14.length() + 6));
            if (id.length() < 20) id = String.format("%-20s", id).replace(' ', '0');
        }
        return id;
    }
}
