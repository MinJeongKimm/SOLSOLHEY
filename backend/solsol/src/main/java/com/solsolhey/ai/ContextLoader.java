package com.solsolhey.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solsolhey.ai.context.AcademicCommon;
import com.solsolhey.ai.context.UserContext;
import com.solsolhey.ai.model.AcademicContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ContextLoader {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final ObjectMapper mapper = new ObjectMapper();

    private UserContext userA;
    private UserContext userB;
    private AcademicCommon academicCommon;

    public ContextLoader() {
        // Lazy loading; will attempt on first access
    }

    public Optional<UserContext> getUserContextById(Long userId) {
        loadIfNeeded();
        if (userId == null) return Optional.empty();
        // 간단 매핑: 짝수 → userA, 홀수 → userB (데모용)
        boolean even = (userId % 2L) == 0L;
        UserContext chosen = even ? (userA != null ? userA : userB) : (userB != null ? userB : userA);
        return Optional.ofNullable(chosen);
    }

    public Optional<AcademicContext> getAcademicContext() {
        loadIfNeeded();
        if (academicCommon == null) return Optional.empty();
        try {
            LocalDate today = academicCommon.today != null ? LocalDate.parse(academicCommon.today) : LocalDate.now();
            var events = new java.util.ArrayList<AcademicContext.Event>();
            if (academicCommon.exam_period != null) {
                if (academicCommon.exam_period.start != null) {
                    LocalDateTime start = LocalDate.parse(academicCommon.exam_period.start).atTime(LocalTime.of(9, 0));
                    events.add(new AcademicContext.Event("중간고사 시작", start.format(ISO), "EXAM", "중간고사"));
                }
                if (academicCommon.exam_period.end != null) {
                    LocalDateTime end = LocalDate.parse(academicCommon.exam_period.end).atTime(LocalTime.of(18, 0));
                    events.add(new AcademicContext.Event("중간고사 종료", end.format(ISO), "EXAM", "중간고사"));
                }
            }
            if (academicCommon.semester_end != null) {
                LocalDateTime end = LocalDate.parse(academicCommon.semester_end).atTime(LocalTime.of(23, 59));
                events.add(new AcademicContext.Event("종강", end.format(ISO), "SEMESTER", "학기"));
            }

            // 확장 섹션 병합: upcoming_events, today_schedule, notices
            if (academicCommon.upcoming_events != null) {
                for (var e : academicCommon.upcoming_events) {
                    if (e == null) continue;
                    events.add(new AcademicContext.Event(
                            nvl(e.title, "일정"), nvl(e.dueAtISO, today.atTime(23,59).format(ISO)),
                            nvl(e.category, "EVENT"), nvl(e.course, "")));
                }
            }

            var schedule = new java.util.ArrayList<AcademicContext.Today>();
            if (academicCommon.today_schedule != null) {
                for (var t : academicCommon.today_schedule) {
                    if (t == null) continue;
                    schedule.add(new AcademicContext.Today(
                            nvl(t.name, "일정"), nvl(t.startAtISO, today.atTime(9,0).format(ISO)),
                            nvl(t.endAtISO, today.atTime(18,0).format(ISO)), nvl(t.location, "")));
                }
            }

            var notices = new java.util.ArrayList<AcademicContext.Notice>();
            if (academicCommon.notices != null) {
                for (var n : academicCommon.notices) {
                    if (n == null) continue;
                    notices.add(new AcademicContext.Notice(nvl(n.title, "공지"), nvl(n.postedAtISO, today.atStartOfDay().format(ISO))));
                }
            }

            return Optional.of(new AcademicContext(events, schedule, notices));
        } catch (Exception e) {
            log.warn("학사 공통 데이터 파싱 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getUserSummary(Long userId) {
        return getUserContextById(userId).map(this::summarizeUser);
    }

    public Optional<UserFacets> getUserFacets(Long userId) {
        return getUserContextById(userId).map(this::extractFacets);
    }

    private String summarizeUser(UserContext u) {
        StringBuilder sb = new StringBuilder();
        if (u.major_group != null && !u.major_group.isBlank()) {
            sb.append("major=").append(u.major_group);
        }
        if (u.role_tags != null && !u.role_tags.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("role=").append(String.join("/", u.role_tags));
        }
        if (u.timetable != null) {
            String timePref = null;
            if (Boolean.TRUE.equals(u.timetable.morning) && Boolean.TRUE.equals(u.timetable.afternoon)) timePref = "오전/오후";
            else if (Boolean.TRUE.equals(u.timetable.morning)) timePref = "오전";
            else if (Boolean.TRUE.equals(u.timetable.afternoon)) timePref = "오후";
            if (timePref != null) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append("time=").append(timePref);
            }
            if (Boolean.TRUE.equals(u.timetable.back_to_back)) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append("pattern=백투백");
            }
            if (Boolean.TRUE.equals(u.timetable.long_free_day)) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append("pattern=긴공강");
            }
        }
        if (u.library != null && u.library.streak_days != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("streak=").append(u.library.streak_days).append("일");
        }
        return sb.toString();
    }

    private UserFacets extractFacets(UserContext u) {
        UserFacets f = new UserFacets();
        // major
        if (u.major_group != null && !u.major_group.isBlank()) {
            f.major = u.major_group;
            // simple domain hints
            if (u.major_group.contains("미대")) f.majorHint = "작업/실습";
            else if (u.major_group.contains("공대")) f.majorHint = "문제/리포트";
        }
        // role
        if (u.role_tags != null && !u.role_tags.isEmpty()) {
            f.role = String.join("/", u.role_tags);
            if (u.role_tags.contains("freshman")) f.roleHint = "기초 다지기";
            if (u.role_tags.contains("graduating")) f.roleHint = "졸업 준비";
        }
        // time/pattern
        if (u.timetable != null) {
            if (Boolean.TRUE.equals(u.timetable.morning) && !Boolean.TRUE.equals(u.timetable.afternoon)) f.time = "오전";
            else if (!Boolean.TRUE.equals(u.timetable.morning) && Boolean.TRUE.equals(u.timetable.afternoon)) f.time = "오후";
            else if (Boolean.TRUE.equals(u.timetable.morning) && Boolean.TRUE.equals(u.timetable.afternoon)) f.time = "오전/오후";

            if (Boolean.TRUE.equals(u.timetable.back_to_back)) f.pattern = "백투백";
            else if (Boolean.TRUE.equals(u.timetable.long_free_day)) f.pattern = "긴공강";
        }
        // streak
        if (u.library != null && u.library.streak_days != null) {
            f.streak = u.library.streak_days;
        }
        return f;
    }

    public boolean userNotesSuggestAvoidSocial(Long userId) {
        return getUserContextById(userId)
                .map(u -> u.notes != null && u.notes.contains("소셜") && u.notes.contains("회피"))
                .orElse(false);
    }

    private void loadIfNeeded() {
        if (userA == null || userB == null || academicCommon == null) {
            try {
                // 0) 단일 파일 우선 (documents/data 또는 classpath)
                Combined maybeCombined = readCombinedIfExists();
                if (maybeCombined != null) {
                    if (maybeCombined.academic != null) academicCommon = maybeCombined.academic;
                    if (maybeCombined.userA != null) userA = maybeCombined.userA;
                    if (maybeCombined.userB != null) userB = maybeCombined.userB;
                }

                // 1) documents/data 개별 파일 우선
                File docDir = new File("documents/data");
                if (docDir.exists()) {
                    File ua = new File(docDir, "userA.json");
                    File ub = new File(docDir, "userB.json");
                    File ac = new File(docDir, "academic_common.json");
                    if (userA == null && ua.exists()) userA = mapper.readValue(Files.readAllBytes(ua.toPath()), UserContext.class);
                    if (userB == null && ub.exists()) userB = mapper.readValue(Files.readAllBytes(ub.toPath()), UserContext.class);
                    if (academicCommon == null && ac.exists()) academicCommon = mapper.readValue(Files.readAllBytes(ac.toPath()), AcademicCommon.class);
                }

                // 2) classpath:ai/ 폴백 (단일 파일 먼저)
                if (userA == null || userB == null || academicCommon == null) {
                    Combined cp = readClasspathJson("ai/combined.json", Combined.class);
                    if (cp != null) {
                        if (academicCommon == null) academicCommon = cp.academic;
                        if (userA == null) userA = cp.userA;
                        if (userB == null) userB = cp.userB;
                    }
                }
                if (userA == null) userA = readClasspathJson("ai/userA.json", UserContext.class);
                if (userB == null) userB = readClasspathJson("ai/userB.json", UserContext.class);
                if (academicCommon == null) academicCommon = readClasspathJson("ai/academic_common.json", AcademicCommon.class);
            } catch (IOException e) {
                log.warn("컨텍스트 로딩 실패: {}", e.getMessage());
            }
        }
    }

    private <T> T readClasspathJson(String path, Class<T> type) {
        try {
            var res = new ClassPathResource(path);
            if (res.exists()) {
                try (var in = res.getInputStream()) {
                    return mapper.readValue(in, type);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String nvl(String v, String d) { return (v == null || v.isBlank()) ? d : v; }

    private Combined readCombinedIfExists() {
        try {
            File docDir = new File("documents/data");
            File combinedInDoc = new File(docDir, "combined.json");
            if (combinedInDoc.exists()) {
                return mapper.readValue(Files.readAllBytes(combinedInDoc.toPath()), Combined.class);
            }
            // classpath will be tried later in loadIfNeeded (so return null here)
        } catch (Exception ignored) {}
        return null;
    }

    // 단일 파일(JSON) 파싱용 컨테이너
    public static class Combined {
        public AcademicCommon academic;
        public UserContext userA;
        public UserContext userB;
    }
}

class UserFacets {
    public String major;      // 예: 미대, 공대
    public String majorHint;  // 예: 작업/실습, 문제/리포트
    public String role;       // 예: freshman, graduating
    public String roleHint;   // 예: 기초 다지기, 졸업 준비
    public String time;       // 예: 오전, 오후, 오전/오후
    public String pattern;    // 예: 백투백, 긴공강
    public Integer streak;    // 예: 21
}
