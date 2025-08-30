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
    private enum FocusType { ACADEMIC, TIME, PATTERN, MAJOR, ROLE, STREAK }
    private final ConcurrentHashMap<Long, java.util.Deque<FocusType>> recentFocusByUser = new ConcurrentHashMap<>();

    public com.solsolhey.ai.dto.AiSpeechGenResult generateSpeech(Long userId) {
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
            var focus = chooseFocus(userId, desired);
            result = tryAcademic(campus, nickname, level, userId, avoidSocial, userSummary, focus);
            if (result != null && !result.isBlank()) {
                produced = SpeechType.ACADEMIC;
            } else {
                focus = chooseFocus(userId, SpeechType.CHALLENGE);
                result = tryChallenge(campus, nickname, level, userId, avoidSocial, userSummary, focus);
                if (result != null && !result.isBlank()) produced = SpeechType.CHALLENGE;
            }
        } else { // desired CHALLENGE
            var focus = chooseFocus(userId, desired);
            result = tryChallenge(campus, nickname, level, userId, avoidSocial, userSummary, focus);
            if (result != null && !result.isBlank()) {
                produced = SpeechType.CHALLENGE;
            } else {
                focus = chooseFocus(userId, SpeechType.ACADEMIC);
                result = tryAcademic(campus, nickname, level, userId, avoidSocial, userSummary, focus);
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
        finalText = ensureMaxLength(finalText, 25);
        if (userId != null && produced != null) {
            lastTypeByUser.put(userId, produced);
        }
        String kind = produced == null ? SpeechType.ACADEMIC.name() : produced.name();
        return new com.solsolhey.ai.dto.AiSpeechGenResult(finalText, kind);
    }

    private String tryAcademic(String campus, String nickname, Integer level, Long userId, boolean avoidSocial, String userSummary, FocusType focus) {
        AcademicContext academic = contextLoader.getAcademicContext().orElseGet(dummyProvider::getDummyContext);
        AcademicContext varied = selectRandomSubContext(academic);
        String[] focusKV = resolveFocusKV(userId, focus, varied);
        String prompt = promptBuilder.buildPrompt(campus, nickname, level, varied, userSummary, focusKV[0], focusKV[1])
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

    private String tryChallenge(String campus, String nickname, Integer level, Long userId, boolean avoidSocial, String userSummary, FocusType focus) {
        try {
            var now = java.time.LocalDateTime.now();
            var challenges = challengeRepository.findAvailableChallenges(now);
            if (challenges == null || challenges.isEmpty()) return null;
            int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(challenges.size());
            var picked = challenges.get(idx);
            String challengeName = picked.getChallengeName();
            String[] focusKV = resolveFocusKV(userId, focus, null);
            String prompt = promptBuilder.buildChallengePrompt(campus, nickname, level, challengeName, userSummary, focusKV[0], focusKV[1])
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
            return ensureMaxLength(String.format("곧 '%s' 마감이야. 오늘 체크해두면 마음이 한결 편해질 거야!", e.title()), 25);
        }
        return ensureMaxLength("좋은 하루야. 오늘 할 일 한 가지만 정해서 가볍게 시작해보자!", 25);
    }

    private String fallbackChallengeMessage(String challengeName) {
        if (challengeName == null || challengeName.isBlank()) {
            return ensureMaxLength("오늘 할 일 하나 찜해볼까?", 25);
        }
        // 간단한 구어체 권유 템플릿
        return ensureMaxLength(challengeName + " 한 번 해볼까?", 25);
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

    // 길이 보정: 25자 초과 시 재작성 시도 → 실패 시 첫 문장 기준 축약
    private String ensureMaxLength(String text, int max) {
        if (text == null) return "";
        String s = text.trim();
        if (s.length() <= max) return s;
        // 1) 모델에게 축약 요청 시도
        try {
            String prompt = "다음 문장을 의미 유지한 채 한국어 반말 한 문장으로 " + max + "자 이내로 축약해줘. 따옴표 없이 문장만 출력.\n문장: " + s;
            GenerateContentResponse res = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );
            String out = (res == null) ? null : res.text();
            if (out != null) {
                out = normalizeBanmal(out);
                if (out.length() <= max) return out;
            }
        } catch (Exception ignore) {}
        // 2) 휴리스틱 축약: 첫 줄/첫 문장 기준 자르기
        int end = s.indexOf('\n');
        if (end > 0) s = s.substring(0, end).trim();
        int dot = indexOfFirst(s, '!', '?', '。', '！', '？', '…');
        if (dot > 0) s = s.substring(0, dot + 1).trim();
        if (s.length() <= max) return s;
        // 3) 마지막으로 하드 컷(말줄임표 없이) — 최대한 자연스러운 지점에서 자르기
        return s.substring(0, Math.min(max, s.length()));
    }

    private int indexOfFirst(String s, char... chars) {
        int min = -1;
        for (char c : chars) {
            int i = s.indexOf(c);
            if (i >= 0) min = (min < 0) ? i : Math.min(min, i);
        }
        return min;
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

    private FocusType chooseFocus(Long userId, SpeechType desired) {
        var facetsOpt = contextLoader.getUserFacets(userId);
        var academicOpt = contextLoader.getAcademicContext();
        java.util.List<FocusType> pool = new java.util.ArrayList<>();
        java.util.function.BiConsumer<FocusType,Integer> add = (t,w) -> { for (int i=0;i<w;i++) pool.add(t); };
        if (desired == SpeechType.ACADEMIC) {
            if (academicOpt.isPresent()) add.accept(FocusType.ACADEMIC, 4);
            facetsOpt.ifPresent(f -> {
                if (f.time != null) add.accept(FocusType.TIME, 3);
                if (f.pattern != null) add.accept(FocusType.PATTERN, 3);
                if (f.major != null) add.accept(FocusType.MAJOR, 2);
                if (f.role != null) add.accept(FocusType.ROLE, 2);
                if (f.streak != null) add.accept(FocusType.STREAK, 1);
            });
        } else {
            facetsOpt.ifPresent(f -> {
                if (f.time != null) add.accept(FocusType.TIME, 3);
                if (f.pattern != null) add.accept(FocusType.PATTERN, 3);
                if (f.major != null) add.accept(FocusType.MAJOR, 2);
                if (f.role != null) add.accept(FocusType.ROLE, 2);
                if (f.streak != null) add.accept(FocusType.STREAK, 1);
            });
            if (pool.isEmpty() && academicOpt.isPresent()) add.accept(FocusType.ACADEMIC, 1);
        }
        if (pool.isEmpty()) return null;
        var dq = recentFocusByUser.computeIfAbsent(userId == null ? -1L : userId, k -> new java.util.ArrayDeque<>());
        java.util.Set<FocusType> cooldown = new java.util.HashSet<>(dq);
        java.util.List<FocusType> filtered = pool.stream().filter(f -> !cooldown.contains(f)).toList();
        java.util.List<FocusType> finalPool = filtered.isEmpty() ? pool : filtered;
        int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(finalPool.size());
        FocusType pick = finalPool.get(idx);
        dq.addFirst(pick);
        while (dq.size() > 3) dq.removeLast();
        recentFocusByUser.put(userId == null ? -1L : userId, dq);
        return pick;
    }

    private String[] resolveFocusKV(Long userId, FocusType focus, AcademicContext varied) {
        String key = null, val = null;
        if (focus == null) return new String[]{null, null};
        var facets = contextLoader.getUserFacets(userId).orElse(null);
        switch (focus) {
            case ACADEMIC -> {
                if (varied != null && varied.upcomingEvents() != null && !varied.upcomingEvents().isEmpty()) {
                    var e = varied.upcomingEvents().get(0);
                    key = "academic";
                    val = e.title();
                }
            }
            case TIME -> { if (facets != null && facets.time != null) { key = "time"; val = facets.time; } }
            case PATTERN -> { if (facets != null && facets.pattern != null) { key = "pattern"; val = facets.pattern; } }
            case MAJOR -> { if (facets != null && facets.major != null) { key = "major"; val = facets.major; } }
            case ROLE -> { if (facets != null && facets.role != null) { key = "role"; val = facets.role; } }
            case STREAK -> { if (facets != null && facets.streak != null) { key = "streak"; val = facets.streak + "일"; } }
        }
        return new String[]{key, val};
    }
}
