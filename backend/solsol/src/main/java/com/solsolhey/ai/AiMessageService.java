package com.solsolhey.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.solsolhey.ai.model.AcademicContext;
import com.solsolhey.challenge.repository.ChallengeRepository;
import com.solsolhey.mascot.repository.MascotRepository;
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
    private final MascotRepository mascotRepository;
    private final ChallengeRepository challengeRepository;

    // Vendor rate limit cooldown gate (ms since epoch). When in cooldown, skip model calls and use fallbacks.
    private final java.util.concurrent.atomic.AtomicLong rateLimitUntilMs = new java.util.concurrent.atomic.AtomicLong(0);

    private boolean isRateLimitedNow() {
        return System.currentTimeMillis() < rateLimitUntilMs.get();
    }

    private void onMaybeRateLimited(Exception e) {
        String msg = e == null ? null : String.valueOf(e.getMessage());
        if (msg != null && (msg.contains("429") || msg.toLowerCase().contains("too many requests"))) {
            // Back off for 60 seconds
            rateLimitUntilMs.set(System.currentTimeMillis() + 60_000);
        }
    }

    private enum SpeechType { ACADEMIC, CHALLENGE }
    private enum FocusType { ACADEMIC, TIME, PATTERN, MAJOR, ROLE, STREAK }
    private final ConcurrentHashMap<Long, java.util.Deque<FocusType>> recentFocusByUser = new ConcurrentHashMap<>();

    public com.solsolhey.ai.dto.AiSpeechGenResult generateSpeech(Long userId) {
        var mascot = mascotRepository.findByUserId(userId).orElse(null);
        Integer level = null;
        if (mascot != null) {
            level = mascot.getLevel();
        }
        // 간단 분산: 무작위로 학사/챌린지 중 하나 우선 시도
        SpeechType desired = java.util.concurrent.ThreadLocalRandom.current().nextBoolean() ? SpeechType.ACADEMIC : SpeechType.CHALLENGE;

        String result = null;
        SpeechType produced = null;

        boolean avoidSocial = contextLoader.userNotesSuggestAvoidSocial(userId);
        String userSummary = contextLoader.getUserSummary(userId).orElse(null);

        if (desired == SpeechType.ACADEMIC) {
            var focus = chooseFocus(userId, desired);
            result = tryAcademic(level, userId, avoidSocial, userSummary, focus);
            if (result != null && !result.isBlank()) {
                produced = SpeechType.ACADEMIC;
            } else {
                focus = chooseFocus(userId, SpeechType.CHALLENGE);
                result = tryChallenge(level, userId, avoidSocial, userSummary, focus);
                if (result != null && !result.isBlank()) produced = SpeechType.CHALLENGE;
            }
        } else { // desired CHALLENGE
            var focus = chooseFocus(userId, desired);
            result = tryChallenge(level, userId, avoidSocial, userSummary, focus);
            if (result != null && !result.isBlank()) {
                produced = SpeechType.CHALLENGE;
            } else {
                focus = chooseFocus(userId, SpeechType.ACADEMIC);
                result = tryAcademic(level, userId, avoidSocial, userSummary, focus);
                if (result != null && !result.isBlank()) produced = SpeechType.ACADEMIC;
            }
        }

        if (result == null || result.isBlank()) {
            // 최종 폴백: 학사 기본 멘트
            var academic = dummyProvider.getDummyContext();
            result = fallbackMessage(academic);
            produced = SpeechType.ACADEMIC;
        }

        // 말투 정규화 + 상태 저장
        String finalText = normalizeBanmal(result);
        String kind = produced == null ? SpeechType.ACADEMIC.name() : produced.name();
        return new com.solsolhey.ai.dto.AiSpeechGenResult(finalText, kind);
    }

    private String tryAcademic(Integer level, Long userId, boolean avoidSocial, String userSummary, FocusType focus) {
        // 미리 학사 컨텍스트를 확보하여 레이트리밋/예외 시에도 폴백을 즉시 반환
        AcademicContext academic = contextLoader.getAcademicContext().orElseGet(dummyProvider::getDummyContext);
        if (isRateLimitedNow()) {
            return fallbackMessage(academic);
        }
        AcademicContext varied = selectRandomSubContext(academic);
        String[] focusKV = resolveFocusKV(userId, focus, varied);
        String prompt = promptBuilder.buildPrompt(null, null, level, varied, userSummary, focusKV[0], focusKV[1])
                + (avoidSocial ? "\n소셜 언급 금지" : "");
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
            onMaybeRateLimited(e);
            return fallbackMessage(academic);
        }
        // 모델이 공백을 반환한 경우에도 학사 폴백 사용
        return fallbackMessage(academic);
    }

    private String tryChallenge(Integer level, Long userId, boolean avoidSocial, String userSummary, FocusType focus) {
        if (isRateLimitedNow()) {
            // skip model; fallback to simple challenge message if available
            try {
                var now = java.time.LocalDateTime.now();
                var challenges = challengeRepository.findAvailableChallenges(now);
                if (challenges == null || challenges.isEmpty()) return fallbackChallengeMessage(null);
                int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(challenges.size());
                String challengeName = challenges.get(idx).getChallengeName();
                return fallbackChallengeMessage(challengeName);
            } catch (Exception ignored) {
                return null;
            }
        }
        try {
            var now = java.time.LocalDateTime.now();
            var challenges = challengeRepository.findAvailableChallenges(now);
            if (challenges == null || challenges.isEmpty()) return null;
            int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(challenges.size());
            var picked = challenges.get(idx);
            String challengeName = picked.getChallengeName();
            String[] focusKV = resolveFocusKV(userId, focus, null);
            String prompt = promptBuilder.buildChallengePrompt(null, null, level, challengeName, userSummary, focusKV[0], focusKV[1])
                    + (avoidSocial ? "\n소셜 언급 금지" : "");
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
                onMaybeRateLimited(aiEx);
            }
            return fallbackChallengeMessage(challengeName);
        } catch (Exception listEx) {
            log.warn("챌린지 목록 조회 실패: {}", listEx.getMessage());
            return null;
        }
    }

    private String fallbackMessage(AcademicContext academic) {
        if (academic == null) return "오늘 할 일 하나 찜해볼까?";
        var rnd = java.util.concurrent.ThreadLocalRandom.current();
        boolean hasE = academic.upcomingEvents() != null && !academic.upcomingEvents().isEmpty();
        boolean hasT = academic.todaySchedule() != null && !academic.todaySchedule().isEmpty();
        boolean hasN = academic.notices() != null && !academic.notices().isEmpty();
        if (!hasE && !hasT && !hasN) return "오늘 할 일 하나 찜해볼까?";

        // 무작위로 카테고리 선택 후 한 항목 고름
        java.util.List<Integer> buckets = new java.util.ArrayList<>();
        if (hasE) buckets.add(0);
        if (hasT) buckets.add(1);
        if (hasN) buckets.add(2);
        int pick = buckets.get(rnd.nextInt(buckets.size()));
        switch (pick) {
            case 0 -> {
                int idx = rnd.nextInt(academic.upcomingEvents().size());
                var e = academic.upcomingEvents().get(idx);
                String t = e.title();
                return (t == null || t.isBlank()) ? "마감 하나 체크하자!" : (t + " 마감 곧이야. 오늘 확인하자!");
            }
            case 1 -> {
                int idx = rnd.nextInt(academic.todaySchedule().size());
                var t = academic.todaySchedule().get(idx);
                String name = t.name();
                return (name == null || name.isBlank()) ? "오늘 일정 한 번 보자!" : (name + " 준비하자!");
            }
            default -> {
                int idx = rnd.nextInt(academic.notices().size());
                var n = academic.notices().get(idx);
                String title = n.title();
                return (title == null || title.isBlank()) ? "새 공지 올라왔대!" : (title + " 공지 확인하자");
            }
        }
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

    // 길이 제한 제거: ensureMaxLength/클램프 로직을 사용하지 않습니다.

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
