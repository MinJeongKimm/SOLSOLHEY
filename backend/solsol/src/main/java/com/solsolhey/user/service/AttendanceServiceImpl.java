package com.solsolhey.user.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.point.dto.request.PointEarnRequest;
import com.solsolhey.point.dto.response.PointTransactionResponse;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import com.solsolhey.point.service.PointService;
import com.solsolhey.user.dto.AttendanceRecordDto;
import com.solsolhey.user.entity.Attendance;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.AttendanceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final PointService pointService;

    @Override
    @Transactional(readOnly = true)
    public boolean hasAttendedToday(User user) {
        return attendanceRepository.existsByUserAndAttendanceDate(user, LocalDate.now());
    }

    @Override
    public AttendanceResult checkInToday(User user) {
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByUserAndAttendanceDate(user, today)) {
            // 이미 출석한 경우
            Attendance latest = attendanceRepository.findTopByUserOrderByAttendanceDateDesc(user).orElse(null);
            int consecutive = latest != null ? latest.getConsecutiveDays() : 0;
            return new AttendanceResult(true, consecutive, latest != null ? latest.getExpReward() : 0, latest != null ? latest.getPointReward() : 0);
        }

        // 오늘 출석 처리
        var latestOpt = attendanceRepository.findTopByUserOrderByAttendanceDateDesc(user);
        int consecutive = 1;

        if (latestOpt.isPresent()) {
            LocalDate lastDate = latestOpt.get().getAttendanceDate();
            if (lastDate.equals(today.minusDays(1))) {
                consecutive = latestOpt.get().getConsecutiveDays() + 1;
            }
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .attendanceDate(today)
                .consecutiveDays(consecutive)
                .expReward(null)  // 자동 계산되도록 null 전달
                .pointReward(null) // 자동 계산되도록 null 전달
                .build();

        attendanceRepository.save(attendance);

        // 포인트 지급 - PointEarnRequest 객체 생성하여 전달 (DAILY_BONUS 타입으로 설정하여 일일 한도에서 제외)
        PointTransactionResponse tx = pointService.earnPoints(user, new PointEarnRequest(10, "출석체크", user.getUserId(), ReferenceType.DAILY_BONUS));

        return new AttendanceResult(true, attendance.getConsecutiveDays(), attendance.getExpReward(), tx.pointAmount());
    }

    @Override
    public List<AttendanceRecordDto> getAttendanceRecords(User user, LocalDate startDate, LocalDate endDate) {
        // 기본값 설정: startDate가 null이면 1년 전, endDate가 null이면 오늘
        LocalDate defaultStartDate = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate defaultEndDate = endDate != null ? endDate : LocalDate.now();

        List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            user, defaultStartDate, defaultEndDate
        );

        return attendances.stream()
                .map(attendance -> new AttendanceRecordDto(
                    attendance.getAttendanceDate(),
                    attendance.getConsecutiveDays()
                ))
                .toList();
    }
}

