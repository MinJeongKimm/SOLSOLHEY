package com.solsolhey.user.service;

import com.solsolhey.user.entity.User;

public interface AttendanceService {

    /**
     * 오늘 출석 여부 확인
     */
    boolean hasAttendedToday(User user);

    /**
     * 오늘 출석 처리 및 보상 지급
     */
    AttendanceResult checkInToday(User user);

    record AttendanceResult(boolean attended, int consecutiveDays, int expReward, int pointReward) {}
}

