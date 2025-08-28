package com.solsolhey.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.solsolhey.ai.model.AcademicContext;
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
}

