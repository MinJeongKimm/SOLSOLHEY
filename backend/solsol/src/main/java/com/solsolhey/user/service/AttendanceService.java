package com.solsolhey.user.service;

import java.time.LocalDate;
import java.util.List;

import com.solsolhey.user.dto.AttendanceRecordDto;
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
    
    /**
     * 기간별 출석 기록 조회
     */
    List<AttendanceRecordDto> getAttendanceRecords(User user, LocalDate startDate, LocalDate endDate);

    record ExpAwardedView(Integer amount, String type, String category, Integer totalExp, Integer level) {}
    record AttendanceResult(boolean attended, int consecutiveDays, int pointReward, ExpAwardedView expAwarded) {}
}

