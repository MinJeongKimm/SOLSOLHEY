package com.solsolhey.finance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class FinanceWebClientConfig {
    
    private final FinanceApiProperties properties;
    
    @Bean("financeWebClient")
    public WebClient financeWebClient() {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .filter(loggingFilter())
                .filter(errorHandlingFilter())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
    
    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("외부 API 요청: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
    
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
