package com.solsolhey.ai.model;

import java.util.List;

public record AcademicContext(
        List<Event> upcomingEvents,
        List<Today> todaySchedule,
        List<Notice> notices
) {
    public record Event(String title, String dueAtISO, String category, String course) {}
    public record Today(String name, String startAtISO, String endAtISO, String location) {}
    public record Notice(String title, String postedAtISO) {}
}

