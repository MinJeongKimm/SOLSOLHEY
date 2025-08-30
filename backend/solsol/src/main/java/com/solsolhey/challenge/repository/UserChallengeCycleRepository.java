package com.solsolhey.challenge.repository;

import com.solsolhey.challenge.entity.Challenge;
import com.solsolhey.challenge.entity.UserChallengeCycle;
import com.solsolhey.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserChallengeCycleRepository extends JpaRepository<UserChallengeCycle, Long> {

    Optional<UserChallengeCycle> findByUserAndChallengeAndCycleTypeAndCycleKeyDate(
            User user, Challenge challenge, Challenge.ResetPolicy cycleType, LocalDate cycleKeyDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserChallengeCycle> findWithLockByUserAndChallengeAndCycleTypeAndCycleKeyDate(
            User user, Challenge challenge, Challenge.ResetPolicy cycleType, LocalDate cycleKeyDate);

    Optional<UserChallengeCycle> findTopByUserAndChallengeAndCycleTypeOrderByStartedAtDesc(
            User user, Challenge challenge, Challenge.ResetPolicy cycleType);

    Optional<UserChallengeCycle> findTopByUserAndChallengeAndCycleTypeOrderByCompletedAtDesc(
            User user, Challenge challenge, Challenge.ResetPolicy cycleType);
}
