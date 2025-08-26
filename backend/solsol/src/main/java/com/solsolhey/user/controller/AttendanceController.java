package com.solsolhey.user.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.user.dto.AttendanceRecordDto;
import com.solsolhey.user.service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance API", description = "출석 체크 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "오늘 출석 여부", description = "오늘 출석했는지 여부를 반환합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hasToday(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean attended = attendanceService.hasAttendedToday(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success("OK", Map.of("attended", attended)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출석 체크", description = "오늘 출석을 기록하고 보상을 지급합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkIn(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            var result = attendanceService.checkInToday(userDetails.getUser());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success("출석 완료", Map.of(
                            "attended", result.attended(),
                            "consecutiveDays", result.consecutiveDays(),
                            "expReward", result.expReward(),
                            "pointReward", result.pointReward()
                    )));
        } catch (Exception e) {
            log.error("출석 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError("출석 처리 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/records")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "출석 기록 조회", description = "기간별 출석 기록을 조회합니다")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> getAttendanceRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            List<AttendanceRecordDto> records = attendanceService.getAttendanceRecords(
                userDetails.getUser(), startDate, endDate
            );
            return ResponseEntity.ok(ApiResponse.success("출석 기록 조회 성공", records));
        } catch (Exception e) {
            log.error("출석 기록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError("출석 기록 조회 중 오류가 발생했습니다."));
        }
    }
}

