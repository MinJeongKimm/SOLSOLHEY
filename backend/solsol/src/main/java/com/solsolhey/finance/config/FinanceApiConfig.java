package com.solsolhey.finance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
@Slf4j
public class FinanceApiConfig {
    
    @Value("${finance.api.base-url:https://finopenapi.ssafy.io/ssafy/api/v1/edu}")
    private String baseUrl;
    
    @Value("${finance.api.timeout:10}")
    private int timeoutSeconds;
    
    @Value("${finance.api.key:}")
    private String apiKey;
    
    @Bean("financeWebClient")
    public WebClient financeWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(loggingFilter())
                .filter(errorHandlingFilter())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024));
        
        // API 키가 있으면 기본 헤더에 추가
        if (apiKey != null && !apiKey.isEmpty()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }
        
        return builder.build();
    }
    
    /**
     * 로깅 필터
     */
    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("외부 API 요청: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
    
    /**
     * 에러 처리 필터
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.error("외부 API 응답 오류: {} {}", 
                    clientResponse.statusCode().value(), 
                    clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}
