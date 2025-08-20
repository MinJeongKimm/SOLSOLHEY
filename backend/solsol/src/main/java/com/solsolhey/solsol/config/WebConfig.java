package com.solsolhey.solsol.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import com.solsolhey.solsol.common.security.RateLimitingFilter;  // 임시 주석 처리
import lombok.RequiredArgsConstructor;
// import org.springframework.boot.web.servlet.FilterRegistrationBean;  // 임시 주석 처리
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    // private final RateLimitingFilter rateLimitingFilter;  // 임시 주석 처리

    /**
     * ObjectMapper Bean 설정
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Rate Limiting 필터 등록 (임시 비활성화 - 디버깅용)
     * Spring Security 필터보다 먼저 실행되도록 높은 우선순위 설정
     */
    // @Bean  // 임시로 주석 처리 - Rate Limiting으로 인한 서버 시작 문제 해결
    /*
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitingFilter);  
        registration.addUrlPatterns("/api/*", "/auth/*");  // API 경로에만 적용
        registration.setOrder(1);  // 가장 높은 우선순위 (Security 필터보다 먼저)
        registration.setName("rateLimitingFilter");
        return registration;
    }
    */
}
