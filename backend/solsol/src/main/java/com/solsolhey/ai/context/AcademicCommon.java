package com.solsolhey.ai.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcademicCommon {
    public String today; // YYYY-MM-DD
    public String timezone; // IANA TZ e.g., Asia/Seoul
    public Period exam_period;
    public String semester_end;
    public String vacation_start;

    // 확장: 풍부한 학사 데이터(옵션)
    public java.util.List<EventDTO> upcoming_events;   // 임박 이벤트 목록
    public java.util.List<TodayDTO> today_schedule;    // 오늘 일정
    public java.util.List<NoticeDTO> notices;          // 공지사항

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Period {
        public String start; // YYYY-MM-DD
        public String end;   // YYYY-MM-DD
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventDTO {
        public String title;      // 예: "과제 제출"
        public String dueAtISO;   // YYYY-MM-DDTHH:mm:ss
        public String category;   // 예: ASSIGNMENT/EXAM/NOTICE 등
        public String course;     // 예: 과목명
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TodayDTO {
        public String name;       // 예: "자료구조 강의"
        public String startAtISO; // YYYY-MM-DDTHH:mm:ss
        public String endAtISO;   // YYYY-MM-DDTHH:mm:ss
        public String location;   // 예: "공대 302호"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NoticeDTO {
        public String title;      // 예: "수강신청 변경 안내"
        public String postedAtISO;// YYYY-MM-DDTHH:mm:ss
    }
}
