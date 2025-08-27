package com.solsolhey.exp.repository;

import com.solsolhey.exp.entity.ExpDailyCounter;
import com.solsolhey.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExpDailyCounterRepository extends JpaRepository<ExpDailyCounter, Long> {

    Optional<ExpDailyCounter> findByUserAndCounterDate(User user, LocalDate counterDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExpDailyCounter> findWithLockByUserAndCounterDate(User user, LocalDate counterDate);
}

