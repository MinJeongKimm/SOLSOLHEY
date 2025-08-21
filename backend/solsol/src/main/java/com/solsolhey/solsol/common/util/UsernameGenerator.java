package com.solsolhey.solsol.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 사용자명 자동 생성 유틸리티
 * 외부 금융 API 요구사항에 맞춰 이메일 앞부분을 그대로 사용
 */
@Component
@Slf4j
public class UsernameGenerator {

    /**
     * 이메일에서 username 생성 (중복 체크 없음)
     * 외부 금융 API에서 이메일과 그 앞부분을 요구하므로 항상 이메일 앞부분 사용
     */
    public String generateUsername(String email) {
        String username = extractLocalPart(email);
        log.info("생성된 username: {} (원본 이메일: {})", username, email);
        return username;
    }

    /**
     * 이메일의 로컬 파트 추출
     */
    private String extractLocalPart(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + email);
        }
        
        String localPart = email.split("@")[0];
        
        // 특수문자 제거 및 길이 제한
        localPart = localPart.replaceAll("[^a-zA-Z0-9]", "");
        
        // 최소 길이 보장
        if (localPart.length() < 3) {
            localPart = localPart + "user";
        }
        
        // 최대 길이 제한
        if (localPart.length() > 20) {
            localPart = localPart.substring(0, 20);
        }
        
        return localPart;
    }
}
