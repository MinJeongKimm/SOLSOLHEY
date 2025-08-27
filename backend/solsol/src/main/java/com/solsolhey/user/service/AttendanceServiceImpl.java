package com.solsolhey.user.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.exp.service.ExpDailyCounterService;
import com.solsolhey.point.dto.response.PointTransactionResponse;
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
    private final ExpDailyCounterService expDailyCounterService;

    @Override
    @Transactional(readOnly = true)
    public boolean hasAttendedToday(User user) {
        return attendanceRepository.existsByUserAndAttendanceDate(user, LocalDate.now());
    }

    @Override
    public AttendanceResult checkInToday(User user) {
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByUserAndAttendanceDate(user, today)) {
            // 이미 출석한 경우: 연속 일수는 최신 기록 기준, EXP 재적립 없음
            Attendance latest = attendanceRepository.findTopByUserOrderByAttendanceDateDesc(user).orElse(null);
            int consecutive = latest != null ? latest.getConsecutiveDays() : 1;
            return new AttendanceResult(true, consecutive, latest != null ? latest.getPointReward() : 0, null);
        }

        // 오늘 출석 처리(연속 일수 계산)
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
                .expReward(null)   // 엔티티에서 연속일수 기반 자동 계산
                .pointReward(null) // 엔티티에서 연속일수 기반 자동 계산
                .build();

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약(사용자+날짜) 충돌 시 이미 출석 처리된 것으로 간주
            log.warn("동시성으로 인한 출석 중복 저장: userId={}", user.getUserId());
        }

        // 포인트 지급(일일 보너스 중복 방지 로직 내장)
        PointTransactionResponse tx = pointService.giveDailyBonus(user);

        // EXP 적립 (일일 카운터)
        var awardedOpt = expDailyCounterService.awardAttendanceExp(user, consecutive);
        ExpAwardedView awardedView = awardedOpt
                .map(a -> new ExpAwardedView(a.amount(), a.type(), a.category(), a.totalExp(), a.level()))
                .orElse(null);

        return new AttendanceResult(true, consecutive, tx.pointAmount(), awardedView);
    }

    @Override
    public List<AttendanceRecordDto> getAttendanceRecords(User user, LocalDate startDate, LocalDate endDate) {
        LocalDate defaultStartDate = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate defaultEndDate = endDate != null ? endDate : LocalDate.now();

        List<Attendance> attendances = attendanceRepository
                .findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(user, defaultStartDate, defaultEndDate);

        return attendances.stream()
                .map(a -> new AttendanceRecordDto(a.getAttendanceDate(), a.getConsecutiveDays()))
                .toList();
    }
}

