package com.solsolhey.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class EnvironmentConfig {
    
    @Autowired
    private Environment environment;
    
    @PostConstruct
    public void logEnvironmentVariables() {
        try {
            // Spring Boot가 이미 .env 파일을 로드했으므로 로깅만 수행
            log.info("Spring Boot 환경변수 로드 완료");
            log.debug("활성 프로파일: {}", String.join(", ", environment.getActiveProfiles()));
            
            // 주요 환경변수들 로깅 (민감한 정보는 마스킹)
            logEnvironmentVariable("DB_HOST");
            logEnvironmentVariable("DB_PORT");
            logEnvironmentVariable("DB_NAME");
            logEnvironmentVariable("JWT_SECRET_KEY");
            logEnvironmentVariable("FINANCE_API_KEY");
            
        } catch (Exception e) {
            log.warn("환경변수 로깅 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 환경변수 값을 안전하게 로깅
     */
    private void logEnvironmentVariable(String key) {
        String value = environment.getProperty(key);
        if (value != null) {
            log.debug("환경변수 {} = {}", key, maskSensitiveValue(key, value));
        } else {
            log.debug("환경변수 {} = 설정되지 않음", key);
        }
    }
    
    /**
     * 민감한 정보는 마스킹 처리
     */
    private String maskSensitiveValue(String key, String value) {
        if (key.toLowerCase().contains("key") || 
            key.toLowerCase().contains("secret") || 
            key.toLowerCase().contains("password") ||
            key.toLowerCase().contains("token")) {
            return value.length() > 4 ? 
                value.substring(0, 4) + "*".repeat(value.length() - 4) : 
                "*".repeat(value.length());
        }
        return value;
    }
}
