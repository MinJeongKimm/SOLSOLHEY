package com.solsolhey.ai;

import com.solsolhey.ai.model.AcademicContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PromptBuilder {
    public String buildPrompt(String campusName, String nickname, Integer level, AcademicContext academic) {
        return buildPrompt(campusName, nickname, level, academic, null, null, null);
    }

    public String buildPrompt(String campusName, String nickname, Integer level, AcademicContext academic, String userSummary, String focusKey, String focusValue) {
        StringBuilder sb = new StringBuilder();
        // 핵심 제약만 간결히 명시
        sb.append("한국어 반말, 1문장, 최대 25자. 닉네임/캠퍼스/이모지/따옴표 금지. 친근하고 가볍게 동기 유도.\n");
        // 개인화 힌트(간단)
        sb.append("레벨=").append(level == null ? 1 : level).append(".\n");
        if (userSummary != null && !userSummary.isBlank()) sb.append("요약:").append(userSummary).append("\n");
        if (focusKey != null && !focusKey.isBlank() && focusValue != null && !focusValue.isBlank()) sb.append("포커스:").append(focusKey).append("=").append(focusValue).append("\n");
        // 데이터 요약 (필요 최소)
        if (academic != null) {
            sb.append("데이터 요약:\n");
            if (academic.upcomingEvents() != null && !academic.upcomingEvents().isEmpty()) {
                var e = academic.upcomingEvents().get(0);
                sb.append("임박 이벤트: ").append(e.category()).append(" / ")
                        .append(e.title()).append(" / due ")
                        .append(e.dueAtISO()).append("\n");
            }
            if (academic.todaySchedule() != null && !academic.todaySchedule().isEmpty()) {
                var t = academic.todaySchedule().get(0);
                sb.append("오늘 일정: ").append(t.name()).append(" / ")
                        .append(t.startAtISO()).append("~").append(t.endAtISO()).append("\n");
            }
            if (academic.notices() != null && !academic.notices().isEmpty()) {
                var n = academic.notices().get(0);
                sb.append("공지: ").append(n.title()).append(" / ")
                        .append(n.postedAtISO()).append("\n");
            }
        }
        sb.append("출력: 문장만 출력.");
        return sb.toString();
    }

    /**
     * 챌린지 권유 특화 프롬프트
     * - 말풍선용 1문장, 20~25자(최대 25자), 자연스럽게 권유 어조
     */
    public String buildChallengePrompt(String campusName, String nickname, Integer level, String challengeName) {
        return buildChallengePrompt(campusName, nickname, level, challengeName, null, null, null);
    }

    public String buildChallengePrompt(String campusName, String nickname, Integer level, String challengeName, String userSummary, String focusKey, String focusValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("한국어 반말, 1문장, 최대 25자. 닉네임/캠퍼스/이모지/따옴표 금지.\n");
        sb.append("레벨=").append(level == null ? 1 : level).append(".\n");
        if (userSummary != null && !userSummary.isBlank()) sb.append("요약:").append(userSummary).append("\n");
        if (focusKey != null && !focusKey.isBlank() && focusValue != null && !focusValue.isBlank()) sb.append("포커스:").append(focusKey).append("=").append(focusValue).append("\n");
        sb.append("챌린지: ").append(challengeName).append("\n");
        sb.append("출력: 문장만 출력.");
        return sb.toString();
    }
}
