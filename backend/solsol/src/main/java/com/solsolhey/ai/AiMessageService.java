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

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiMessageService {
    private final Client client;
    private final AcademicDummyProvider dummyProvider;
    private final ContextLoader contextLoader;
    private final PromptBuilder promptBuilder;
    private final UserRepository userRepository;
    private final MascotRepository mascotRepository;
    private final ChallengeRepository challengeRepository;

    private enum SpeechType { ACADEMIC, CHALLENGE }
    private final ConcurrentHashMap<Long, SpeechType> lastTypeByUser = new ConcurrentHashMap<>();

    public String generateSpeech(Long userId) {
        var user = userRepository.findById(userId).orElse(null);
        var mascot = mascotRepository.findByUserId(userId).orElse(null);
        String campus = null;
        String nickname = null;
        Integer level = null;
        if (user != null) {
            // PII 금지 정책: 캠퍼스/닉네임은 프롬프트에 사용하지 않음
            campus = null;
            nickname = null;
        }
        if (mascot != null) {
            level = mascot.getLevel();
        }
        // 번갈아가며 출력: 이전 타입 기준으로 다음 타입 결정(기본은 학사 먼저)
        SpeechType prev = lastTypeByUser.getOrDefault(userId, SpeechType.CHALLENGE);
        SpeechType desired = (prev == SpeechType.ACADEMIC) ? SpeechType.CHALLENGE : SpeechType.ACADEMIC;

        String result = null;
        SpeechType produced = null;

        boolean avoidSocial = contextLoader.userNotesSuggestAvoidSocial(userId);
        String userSummary = contextLoader.getUserSummary(userId).orElse(null);

        if (desired == SpeechType.ACADEMIC) {
            result = tryAcademic(campus, nickname, level, userId, avoidSocial, userSummary);
            if (result != null && !result.isBlank()) {
                produced = SpeechType.ACADEMIC;
            } else {
                result = tryChallenge(campus, nickname, level, userId, avoidSocial, userSummary);
                if (result != null && !result.isBlank()) produced = SpeechType.CHALLENGE;
            }
        } else { // desired CHALLENGE
            result = tryChallenge(campus, nickname, level, userId, avoidSocial, userSummary);
            if (result != null && !result.isBlank()) {
                produced = SpeechType.CHALLENGE;
            } else {
                result = tryAcademic(campus, nickname, level, userId, avoidSocial, userSummary);
                if (result != null && !result.isBlank()) produced = SpeechType.ACADEMIC;
            }
        }

        if (result == null || result.isBlank()) {
            // 최종 폴백: 학사 기본 멘트
            var academic = dummyProvider.getDummyContext();
            result = fallbackMessage(campus, nickname, level, academic);
            produced = SpeechType.ACADEMIC;
        }

        // 말투 정규화 + 상태 저장
        String finalText = normalizeBanmal(result);
        if (userId != null && produced != null) {
            lastTypeByUser.put(userId, produced);
        }
        return finalText;
    }

    private String tryAcademic(String campus, String nickname, Integer level, Long userId, boolean avoidSocial, String userSummary) {
        AcademicContext academic = contextLoader.getAcademicContext().orElseGet(dummyProvider::getDummyContext);
        AcademicContext varied = selectRandomSubContext(academic);
        String prompt = promptBuilder.buildPrompt(campus, nickname, level, varied, userSummary)
                + "\n표현 다양화 지침: 같은 의미라도 매번 어휘/어순을 바꿔 자연스럽게 다르게 써줘. 반말 유지, 이모지/닉네임/캠퍼스 언급 금지."
                + (avoidSocial ? "\n추가 제약: 소셜 지표/친구/좋아요/팔로워 등 사회적 비교 표현을 언급하지 말 것." : "")
                + "\n변주 토큰: " + java.util.UUID.randomUUID().toString().substring(0, 8);
        try {
            GenerateContentResponse res = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );
            String text = res.text();
            if (text != null && !text.isBlank()) return text.trim();
        } catch (Exception e) {
            log.warn("학사 메시지 생성 실패: {}", e.getMessage());
        }
        return null;
    }

    private String tryChallenge(String campus, String nickname, Integer level, Long userId, boolean avoidSocial, String userSummary) {
        try {
            var now = java.time.LocalDateTime.now();
            var challenges = challengeRepository.findAvailableChallenges(now);
            if (challenges == null || challenges.isEmpty()) return null;
            int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(challenges.size());
            var picked = challenges.get(idx);
            String challengeName = picked.getChallengeName();
            String prompt = promptBuilder.buildChallengePrompt(campus, nickname, level, challengeName, userSummary)
                    + (avoidSocial ? "\n추가 제약: 소셜 지표/친구/좋아요/팔로워 등 언급 금지." : "");
            try {
                GenerateContentResponse res = client.models.generateContent(
                        "gemini-2.5-flash",
                        prompt,
                        null
                );
                String text = res.text();
                if (text != null && !text.isBlank()) return text.trim();
            } catch (Exception aiEx) {
                log.warn("챌린지 메시지 생성 실패: {}", aiEx.getMessage());
            }
            return fallbackChallengeMessage(challengeName);
        } catch (Exception listEx) {
            log.warn("챌린지 목록 조회 실패: {}", listEx.getMessage());
            return null;
        }
    }

    private String fallbackMessage(String campus, String nickname, Integer level, AcademicContext academic) {
        if (academic != null && academic.upcomingEvents() != null && !academic.upcomingEvents().isEmpty()) {
            var e = academic.upcomingEvents().get(0);
            return String.format("곧 '%s' 마감이야. 오늘 체크해두면 마음이 한결 편해질 거야!", e.title());
        }
        return "좋은 하루야. 오늘 할 일 한 가지만 정해서 가볍게 시작해보자!";
    }

    private String fallbackChallengeMessage(String challengeName) {
        if (challengeName == null || challengeName.isBlank()) {
            return "오늘 할 일 하나 찜해볼까?";
        }
        // 간단한 구어체 권유 템플릿
        return challengeName + " 한 번 해볼까?";
    }

    // 간단한 말투 정규화: 존댓말 → 반말로 변환 (휴리스틱)
    private String normalizeBanmal(String text) {
        if (text == null) return "";
        String s = text.trim();
        // 따옴표 제거
        s = s.replace("\n", " ").replaceAll("[“”\"']", "");
        // 문장부호 앞 공백 정리
        s = s.replaceAll("\s+([.!?])", "$1");
        // 대표 종결어 변환
        s = s.replaceAll("입니다([.!?])", "야$1");
        s = s.replaceAll("입니다$", "야");
        s = s.replaceAll("(이에요|예요|에요)([.!?])", "야$2");
        s = s.replaceAll("(이에요|예요|에요)$", "야");
        s = s.replaceAll("해주세요", "해줘");
        s = s.replace("하실래요?", "할래?");
        s = s.replace("하시겠어요?", "할래?");
        s = s.replace("해볼까요?", "해볼까?");
        s = s.replace("어때요?", "어때?");
        s = s.replaceAll("봅시다", "보자");
        s = s.replaceAll("합시다", "하자");
        s = s.replaceAll("하십시오", "해줘");
        s = s.replaceAll("하세요", "해");
        s = s.replaceAll("좋아요", "좋아");
        s = s.replaceAll("봐요", "봐");
        // 문장 끝의 '요' 제거
        s = s.replaceAll("요([.!?])", "$1");
        s = s.replaceAll("요$", "");
        // 공백 정리
        s = s.replaceAll("\s{2,}", " ").trim();
        return s;
    }

    private AcademicContext selectRandomSubContext(AcademicContext ctx) {
        if (ctx == null) return null;
        var rnd = java.util.concurrent.ThreadLocalRandom.current();
        java.util.List<AcademicContext.Event> events = java.util.List.of();
        java.util.List<AcademicContext.Today> todays = java.util.List.of();
        java.util.List<AcademicContext.Notice> notices = java.util.List.of();
        if (ctx.upcomingEvents() != null && !ctx.upcomingEvents().isEmpty()) {
            int idx = rnd.nextInt(ctx.upcomingEvents().size());
            events = java.util.List.of(ctx.upcomingEvents().get(idx));
        }
        if (ctx.todaySchedule() != null && !ctx.todaySchedule().isEmpty()) {
            int idx = rnd.nextInt(ctx.todaySchedule().size());
            todays = java.util.List.of(ctx.todaySchedule().get(idx));
        }
        if (ctx.notices() != null && !ctx.notices().isEmpty()) {
            int idx = rnd.nextInt(ctx.notices().size());
            notices = java.util.List.of(ctx.notices().get(idx));
        }
        return new AcademicContext(events, todays, notices);
    }
}
