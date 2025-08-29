package com.solsolhey.ai;

import com.solsolhey.ai.model.AcademicContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PromptBuilder {
    public String buildPrompt(String campusName, String nickname, Integer level, AcademicContext academic) {
        String timeGreeting = getTimeGreeting(LocalDateTime.now());
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 사용자의 홈 화면에서 마스코트가 말풍선으로 전하는 짧은 인삿말을 생성합니다.\n");
        sb.append("출력 제약: 한국어, 1~2문장, 60~120자, 친근하고 격려하는 톤, 말풍선용 짧은 문장.\n");
        sb.append("컨텍스트: 캠퍼스='").append(campusName == null ? "해이영 캠퍼스" : campusName)
                .append("', 닉네임='").append(nickname == null ? "친구" : nickname).append("', 레벨=")
                .append(level == null ? 1 : level).append(".\n");
        sb.append("시간대 인사: ").append(timeGreeting).append(" 포함.\n");
        sb.append("학사 데이터가 있을 경우 마감 임박(24~48시간) 우선, 다음으로 오늘 일정, 다음으로 공지를 간단히 반영.\n");
        sb.append("학사 데이터가 없거나 비어있으면 일반 인사 + 동기부여 한 줄.\n");
        sb.append("문구는 공손하지만 가볍고, 부담 없이 행동 유도.\n");

        // 간단한 데이터 요약 (모델 힌트)
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

        sb.append("출력: 말풍선에 바로 넣을 문장만 출력. 불필요한 설명/머리말/따옴표는 출력하지 않음.");
        return sb.toString();
    }

    private String getTimeGreeting(LocalDateTime now) {
        int h = now.getHour();
        if (h >= 5 && h < 12) return "좋은 아침";
        if (h >= 12 && h < 18) return "좋은 오후";
        if (h >= 18 && h < 23) return "좋은 저녁";
        return "좋은 밤";
    }
}

