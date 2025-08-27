package com.solsolhey.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.solsolhey.user.entity.Attendance;
import com.solsolhey.user.entity.User;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByUserAndAttendanceDate(User user, LocalDate attendanceDate);

    Optional<Attendance> findTopByUserOrderByAttendanceDateDesc(User user);
    
    List<Attendance> findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(
        User user, LocalDate startDate, LocalDate endDate
    );
}

