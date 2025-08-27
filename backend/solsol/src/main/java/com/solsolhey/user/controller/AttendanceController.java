package com.solsolhey.user.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.user.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
            var resp = new java.util.HashMap<String, Object>();
            resp.put("attended", result.attended());
            resp.put("consecutiveDays", result.consecutiveDays());
            resp.put("pointReward", result.pointReward());
            if (result.expAwarded() != null) {
                resp.put("expAwarded", Map.of(
                        "amount", result.expAwarded().amount(),
                        "type", result.expAwarded().type(),
                        "category", result.expAwarded().category(),
                        "totalExp", result.expAwarded().totalExp(),
                        "level", result.expAwarded().level()
                ));
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("출석 완료", resp));
        } catch (Exception e) {
            log.error("출석 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError("출석 처리 중 오류가 발생했습니다."));
        }
    }
}

