package com.solsolhey.ai;

import com.solsolhey.ai.model.AcademicContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AcademicDummyProvider {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AcademicContext getDummyContext() {
        // 임박 일정/시험/공지 등 더미 데이터 구성
        LocalDate today = LocalDate.now();
        LocalDateTime dueTomorrow = today.plusDays(1).atTime(LocalTime.of(23, 59));
        LocalDateTime examNextWed = today.plusWeeks(1).with(java.time.DayOfWeek.WEDNESDAY).atTime(10, 0);

        var events = List.of(
                new AcademicContext.Event("캡스톤 설계 보고서 제출", dueTomorrow.format(ISO), "ASSIGNMENT", "캡스톤 설계"),
                new AcademicContext.Event("데이터베이스 중간고사", examNextWed.format(ISO), "EXAM", "데이터베이스")
        );

        var schedule = List.of(
                new AcademicContext.Today("캡스톤 설계 세미나", today.atTime(15, 0).format(ISO), today.atTime(16, 30).format(ISO), "공대 302호")
        );

        var notices = List.of(
                new AcademicContext.Notice("수강신청 변경 기간 안내", today.minusDays(1).atTime(9, 0).format(ISO))
        );

        return new AcademicContext(events, schedule, notices);
    }
}

