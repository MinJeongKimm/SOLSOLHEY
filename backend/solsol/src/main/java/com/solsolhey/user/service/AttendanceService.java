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

    record ExpAwardedView(Integer amount, String type, String category, Integer totalExp, Integer level) {}
    record AttendanceResult(boolean attended, int consecutiveDays, int pointReward, ExpAwardedView expAwarded) {}
}

