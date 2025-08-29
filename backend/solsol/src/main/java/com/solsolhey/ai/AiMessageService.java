package com.solsolhey.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.solsolhey.ai.model.AcademicContext;
import com.solsolhey.challenge.repository.ChallengeRepository;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiMessageService {
    private final Client client;
    private final AcademicDummyProvider dummyProvider;
    private final PromptBuilder promptBuilder;
    private final UserRepository userRepository;
    private final MascotRepository mascotRepository;
    private final ChallengeRepository challengeRepository;

    public String generateSpeech(Long userId) {
        var user = userRepository.findById(userId).orElse(null);
        var mascot = mascotRepository.findByUserId(userId).orElse(null);
        String campus = null;
        String nickname = null;
        Integer level = null;
        if (user != null) {
            campus = user.getCampus();
            nickname = user.getNickname();
        }
        if (mascot != null) {
            level = mascot.getLevel();
        }
        // 1) 진행 가능한 챌린지 기반 권유 메시지 우선 생성
        try {
            var now = java.time.LocalDateTime.now();
            var challenges = challengeRepository.findAvailableChallenges(now);
            if (challenges != null && !challenges.isEmpty()) {
                int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(challenges.size());
                var picked = challenges.get(idx);
                String challengeName = picked.getChallengeName();
                String prompt = promptBuilder.buildChallengePrompt(campus, nickname, level, challengeName);
                try {
                    GenerateContentResponse res = client.models.generateContent(
                            "gemini-2.5-flash",
                            prompt,
                            null
                    );
                    String text = res.text();
                    if (text != null && !text.isBlank()) return text.trim();
                } catch (Exception aiEx) {
                    log.warn("Gemini 호출 실패(챌린지 권유), 폴백 사용: {}", aiEx.getMessage());
                }
                // AI 실패 또는 빈 응답일 때 간단 폴백
                return fallbackChallengeMessage(challengeName);
            }
        } catch (Exception listEx) {
            log.warn("챌린지 목록 조회 실패, 일반 인삿말로 폴백: {}", listEx.getMessage());
        }

        // 2) 챌린지가 없거나 실패 시: 기존 일반 인삿말 생성
        AcademicContext academic = dummyProvider.getDummyContext();
        String prompt = promptBuilder.buildPrompt(campus, nickname, level, academic);
        try {
            GenerateContentResponse res = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );
            String text = res.text();
            if (text == null || text.isBlank()) throw new IllegalStateException("Empty AI response");
            return text.trim();
        } catch (Exception e) {
            log.warn("Gemini 호출 실패, 폴백 사용: {}", e.getMessage());
            return fallbackMessage(campus, nickname, level, academic);
        }
    }

    private String fallbackMessage(String campus, String nickname, Integer level, AcademicContext academic) {
        String hero = nickname != null ? nickname : "친구";
        String camp = campus != null ? campus : "해이영 캠퍼스";
        if (academic != null && academic.upcomingEvents() != null && !academic.upcomingEvents().isEmpty()) {
            var e = academic.upcomingEvents().get(0);
            return String.format("%s 안녕하세요! %s 일정 체크했어요. 곧 '%s' 마감이에요. 오늘 조금만 더 힘내봐요!", hero, camp, e.title());
        }
        return String.format("%s 반가워요! %s에서 멋진 하루 보내요. 오늘도 작은 한 걸음, 충분해요!", hero, camp);
    }

    private String fallbackChallengeMessage(String challengeName) {
        if (challengeName == null || challengeName.isBlank()) {
            return "오늘 할 일 하나 찜해볼까?";
        }
        // 간단한 구어체 권유 템플릿
        return challengeName + " 한 번 해볼까?";
    }
}
