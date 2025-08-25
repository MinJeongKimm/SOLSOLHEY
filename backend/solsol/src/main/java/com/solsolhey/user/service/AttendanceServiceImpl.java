package com.solsolhey.user.service;

import com.solsolhey.point.service.PointService;
import com.solsolhey.point.dto.response.PointTransactionResponse;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import com.solsolhey.user.entity.Attendance;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
            log.info("이미 오늘 출석함: userId={}", user.getUserId());
            Attendance latest = attendanceRepository.findTopByUserOrderByAttendanceDateDesc(user).orElse(null);
            int consecutive = latest != null ? latest.getConsecutiveDays() : 1;
            return new AttendanceResult(true, consecutive, latest != null ? latest.getExpReward() : 0, latest != null ? latest.getPointReward() : 0);
        }

        int consecutiveDays = 1;
        var latestOpt = attendanceRepository.findTopByUserOrderByAttendanceDateDesc(user);
        if (latestOpt.isPresent()) {
            LocalDate lastDate = latestOpt.get().getAttendanceDate();
            if (ChronoUnit.DAYS.between(lastDate, today) == 1) {
                consecutiveDays = latestOpt.get().getConsecutiveDays() + 1;
            } else {
                consecutiveDays = 1;
            }
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .attendanceDate(today)
                .consecutiveDays(consecutiveDays)
                .build();

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약(사용자+날짜) 충돌 시 이미 출석 처리된 것으로 간주
            log.warn("동시성으로 인한 출석 중복 저장: userId={}", user.getUserId());
        }

        // 포인트 지급 (일일 보너스)
        PointTransactionResponse tx = pointService.giveDailyBonus(user);

        return new AttendanceResult(true, attendance.getConsecutiveDays(), attendance.getExpReward(), tx.pointAmount());
    }
}

