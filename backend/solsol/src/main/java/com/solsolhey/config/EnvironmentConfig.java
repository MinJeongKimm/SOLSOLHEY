package com.solsolhey.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class EnvironmentConfig {
    
    @PostConstruct
    public void loadEnvironmentVariables() {
        try {
            // .env 파일이 있으면 로드, 없으면 시스템 환경변수 사용
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // 프로젝트 루트 디렉토리
                    .filename(".env") // .env 파일명
                    .ignoreIfMissing() // 파일이 없어도 오류 안남
                    .load();
            
            // 환경변수들을 시스템 프로퍼티로 설정 (Spring이 읽을 수 있도록)
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 이미 시스템 환경변수나 JVM 옵션으로 설정된 값이 있으면 우선시
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                    log.debug("환경변수 로드: {} = {}", key, maskSensitiveValue(key, value));
                }
            });
            
            log.info(".env 파일 로드 완료");
            
        } catch (RuntimeException e) {
            log.warn(".env 파일 로드 실패 - 시스템 환경변수 사용: {}", e.getMessage());
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
