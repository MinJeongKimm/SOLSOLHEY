package com.solsolhey.solsol.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 사용자명 자동 생성 유틸리티
 */
@Component
@Slf4j
public class UsernameGenerator {
    
    private static final String[] ADJECTIVES = {
        "행복한", "즐거운", "신나는", "멋진", "아름다운", "훌륭한", "재미있는", "특별한",
        "즐거운", "신나는", "멋진", "아름다운", "훌륭한", "재미있는", "특별한"
    };
    
    private static final String[] NOUNS = {
        "사용자", "친구", "친구", "친구", "친구", "친구", "친구", "친구",
        "사용자", "친구", "친구", "친구", "친구", "친구", "친구", "친구"
    };
    
    private final Random random = new Random();
    
    /**
     * 이메일을 기반으로 사용자명 생성
     * @param email 이메일 주소
     * @return 생성된 사용자명
     */
    public String generateUsername(String email) {
        if (email == null || email.trim().isEmpty()) {
            return generateRandomUsername();
        }
        
        // 이메일에서 @ 앞부분 추출
        String localPart = email.split("@")[0];
        
        // 특수문자 제거 및 길이 제한
        String cleanUsername = localPart.replaceAll("[^a-zA-Z0-9가-힣]", "");
        
        if (cleanUsername.length() > 20) {
            cleanUsername = cleanUsername.substring(0, 20);
        }
        
        if (cleanUsername.isEmpty()) {
            return generateRandomUsername();
        }
        
        log.info("생성된 username: {} (원본 이메일: {})", cleanUsername, email);
        return cleanUsername;
    }
    
    /**
     * 랜덤 사용자명 생성
     * @return 랜덤 사용자명
     */
    private String generateRandomUsername() {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int number = random.nextInt(1000);
        
        String randomUsername = adjective + noun + number;
        log.info("랜덤 username 생성: {}", randomUsername);
        
        return randomUsername;
    }
}
