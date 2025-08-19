package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.Challenge;
import com.solsolhey.solsol.entity.User;
import com.solsolhey.solsol.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 챌린지 레포지토리
 */
@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    /**
     * 사용자와 챌린지로 UserChallenge 조회
     */
    Optional<UserChallenge> findByUserAndChallenge(User user, Challenge challenge);

    /**
     * 사용자의 모든 챌린지 참여 내역 조회
     */
    List<UserChallenge> findByUserOrderByStartedAtDesc(User user);

    /**
     * 사용자의 특정 상태 챌린지 조회
     */
    List<UserChallenge> findByUserAndStatusOrderByStartedAtDesc(User user, UserChallenge.ChallengeStatus status);

    /**
     * 사용자의 진행 중인 챌린지 조회
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user = :user AND uc.status = 'IN_PROGRESS' ORDER BY uc.startedAt DESC")
    List<UserChallenge> findInProgressByUser(@Param("user") User user);

    /**
     * 사용자의 완료된 챌린지 조회
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user = :user AND uc.status = 'COMPLETED' ORDER BY uc.completedAt DESC")
    List<UserChallenge> findCompletedByUser(@Param("user") User user);

    /**
     * 특정 챌린지에 참여한 모든 사용자 조회
     */
    List<UserChallenge> findByChallengeOrderByProgressCountDesc(Challenge challenge);

    /**
     * 특정 챌린지의 완료자 수 조회
     */
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.challenge = :challenge AND uc.status = 'COMPLETED'")
    Long countCompletedByChallenge(@Param("challenge") Challenge challenge);

    /**
     * 특정 챌린지의 참여자 수 조회
     */
    Long countByChallenge(Challenge challenge);

    /**
     * 사용자가 이미 참여한 챌린지인지 확인
     */
    boolean existsByUserAndChallenge(User user, Challenge challenge);

    /**
     * 특정 기간 동안 완료한 챌린지 조회
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user = :user AND uc.status = 'COMPLETED' AND " +
           "uc.completedAt >= :startDate AND uc.completedAt <= :endDate " +
           "ORDER BY uc.completedAt DESC")
    List<UserChallenge> findCompletedByUserInPeriod(@Param("user") User user, 
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);

    /**
     * 만료된 진행 중인 챌린지 조회
     */
    @Query("SELECT uc FROM UserChallenge uc JOIN uc.challenge c WHERE uc.status = 'IN_PROGRESS' AND " +
           "c.endDate < :now")
    List<UserChallenge> findExpiredInProgressChallenges(@Param("now") LocalDateTime now);
}
