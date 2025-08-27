package com.solsolhey.user.dto;

import java.time.LocalDate;

public record AttendanceRecordDto(
    LocalDate attendanceDate,
    int consecutiveDays
) {}
