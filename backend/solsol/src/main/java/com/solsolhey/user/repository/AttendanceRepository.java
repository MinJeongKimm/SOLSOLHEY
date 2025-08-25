package com.solsolhey.user.repository;

import com.solsolhey.user.entity.Attendance;
import com.solsolhey.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByUserAndAttendanceDate(User user, LocalDate attendanceDate);

    Optional<Attendance> findTopByUserOrderByAttendanceDateDesc(User user);
}

