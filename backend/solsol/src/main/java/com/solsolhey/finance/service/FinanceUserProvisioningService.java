package com.solsolhey.finance.service;

import com.solsolhey.auth.event.UserRegisteredEvent;
import com.solsolhey.finance.client.FinanceApiClient;
import com.solsolhey.finance.dto.response.MemberResponse;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceUserProvisioningService {

    private final UserRepository userRepository;
    private final FinanceApiClient financeApiClient;

    /**
     * 회원가입 커밋 후 금융 사용자 생성 트리거 (옵션 B)
     */
    @TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        Long userId = event.getUserId();
        try {
            provisionFinanceUser(userId);
        } catch (Exception e) {
            log.error("금융 사용자 생성 트리거 실패(userId={}) - 다음 재시도에서 처리: {}", userId, e.getMessage());
        }
    }

    /**
     * 주기적 재시도: financeUserKey 미부여 사용자 처리
     */
    @Scheduled(fixedDelay = 60_000)
    public void retryPendingFinanceUsers() {
        List<User> targets = userRepository.findTop100ByFinanceUserKeyIsNullAndIsActiveTrueOrderByCreatedAtAsc();
        for (User u : targets) {
            try {
                provisionFinanceUser(u.getUserId());
            } catch (Exception e) {
                log.warn("금융 사용자 생성 재시도 실패(userId={}): {}", u.getUserId(), e.getMessage());
            }
        }
    }

    /**
     * 단일 사용자 처리 로직 (트랜잭션 분리)
     */
    @Transactional
    public void provisionFinanceUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("금융 사용자 생성: 사용자 없음(userId={})", userId);
            return;
        }
        if (user.getFinanceUserKey() != null && !user.getFinanceUserKey().isBlank()) {
            // 이미 처리됨
            return;
        }

        String financeEmail = buildFinanceEmail(user.getEmail(), user.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());

        // 우선 생성 시도
        String userKey = null;
        try {
            MemberResponse created = financeApiClient.createMember(financeEmail).block();
            if (created != null && created.getUserKey() != null) {
                userKey = created.getUserKey();
            }
        } catch (Exception createEx) {
            log.info("금융 회원 생성 실패(중복 가능성): {} — 조회로 회수 시도", createEx.getMessage());
            try {
                MemberResponse found = financeApiClient.searchMember(financeEmail).block();
                if (found != null && found.getUserKey() != null) {
                    userKey = found.getUserKey();
                }
            } catch (Exception searchEx) {
                log.warn("금융 회원 조회 실패(financeEmail={}): {}", maskEmail(financeEmail), searchEx.getMessage());
            }
        }

        if (userKey == null || userKey.isBlank()) {
            // 다음 재시도 대상로 남김
            return;
        }

        // 저장
        user.setFinanceUserKey(userKey);
        userRepository.save(user);
        log.info("금융 userKey 저장 완료(userId={}, key={})", user.getUserId(), maskKey(userKey));
    }

    /**
     * 동기 방식으로 financeUserKey를 보장하고, 최종 userKey를 반환한다.
     * - 이미 존재하면 그대로 반환
     * - 없으면 생성/조회 로직을 수행한 뒤, DB에서 최신 값을 읽어 반환
     */
    @Transactional
    public String provisionAndGetUserKey(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        if (user.getFinanceUserKey() != null && !user.getFinanceUserKey().isBlank()) {
            return user.getFinanceUserKey();
        }
        try {
            provisionFinanceUser(userId);
        } catch (Exception e) {
            log.warn("동기 finance userKey 발급 실패(userId={}): {}", userId, e.getMessage());
        }
        // 최신 값 재조회
        User refreshed = userRepository.findById(userId).orElse(null);
        return refreshed != null ? refreshed.getFinanceUserKey() : null;
    }

    static String buildFinanceEmail(String email, long createdAtEpochMs) {
        if (email == null || email.isBlank()) return null;
        int at = email.indexOf('@');
        if (at <= 0) return email; // 이상치: 그대로 사용
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);
        if (local.length() > 40) local = local.substring(0, 40);
        return local + "+" + createdAtEpochMs + "@" + domain;
    }

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
}
