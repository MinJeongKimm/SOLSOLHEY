package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.FriendInteraction;
import com.solsolhey.solsol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 친구 상호작용 Repository
 */
@Repository
public interface FriendInteractionRepository extends JpaRepository<FriendInteraction, Long> {

    /**
     * 특정 날짜에 특정 사용자 간의 특정 타입 상호작용 존재 여부 확인
     */
    boolean existsByUserAndTargetUserAndInteractionTypeAndDate(
            User user, User targetUser, FriendInteraction.InteractionType interactionType, Date date);

    /**
     * 특정 날짜에 특정 사용자 간의 특정 타입 상호작용 조회
     */
    Optional<FriendInteraction> findByUserAndTargetUserAndInteractionTypeAndDate(
            User user, User targetUser, FriendInteraction.InteractionType interactionType, Date date);

    /**
     * 사용자가 한 모든 상호작용 조회 (최신순)
     */
    List<FriendInteraction> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 사용자가 받은 모든 상호작용 조회 (최신순)
     */
    List<FriendInteraction> findByTargetUserOrderByCreatedAtDesc(User targetUser);

    /**
     * 특정 사용자 간의 모든 상호작용 조회
     */
    @Query("SELECT fi FROM FriendInteraction fi WHERE " +
           "(fi.user = :user1 AND fi.targetUser = :user2) OR " +
           "(fi.user = :user2 AND fi.targetUser = :user1) " +
           "ORDER BY fi.createdAt DESC")
    List<FriendInteraction> findInteractionsBetweenUsers(
            @Param("user1") User user1, @Param("user2") User user2);

    /**
     * 사용자가 특정 날짜에 한 상호작용들 조회
     */
    List<FriendInteraction> findByUserAndDateOrderByCreatedAtDesc(User user, Date date);

    /**
     * 사용자가 특정 날짜에 받은 상호작용들 조회
     */
    List<FriendInteraction> findByTargetUserAndDateOrderByCreatedAtDesc(User targetUser, Date date);

    /**
     * 특정 타입의 상호작용 통계 - 사용자가 한 횟수
     */
    long countByUserAndInteractionType(User user, FriendInteraction.InteractionType interactionType);

    /**
     * 특정 타입의 상호작용 통계 - 사용자가 받은 횟수
     */
    long countByTargetUserAndInteractionType(User targetUser, FriendInteraction.InteractionType interactionType);

    /**
     * 사용자가 오늘 얻은 총 포인트
     */
    @Query("SELECT COALESCE(SUM(fi.pointsEarned), 0) FROM FriendInteraction fi " +
           "WHERE fi.user = :user AND fi.date = :today")
    Integer getTotalPointsEarnedToday(@Param("user") User user, @Param("today") Date today);

    /**
     * 특정 기간 동안의 상호작용 통계
     */
    @Query("SELECT fi.interactionType, COUNT(fi), COALESCE(SUM(fi.pointsEarned), 0) " +
           "FROM FriendInteraction fi " +
           "WHERE fi.user = :user AND fi.date BETWEEN :startDate AND :endDate " +
           "GROUP BY fi.interactionType")
    List<Object[]> getInteractionStatsByPeriod(
            @Param("user") User user, 
            @Param("startDate") Date startDate, 
            @Param("endDate") Date endDate);

    /**
     * 가장 활발한 상호작용 사용자들 (TOP 10)
     */
    @Query("SELECT fi.targetUser, COUNT(fi) as interactionCount " +
           "FROM FriendInteraction fi " +
           "WHERE fi.user = :user " +
           "GROUP BY fi.targetUser " +
           "ORDER BY interactionCount DESC")
    List<Object[]> getTopInteractionTargets(@Param("user") User user, 
                                          @Param("limit") int limit);
}
