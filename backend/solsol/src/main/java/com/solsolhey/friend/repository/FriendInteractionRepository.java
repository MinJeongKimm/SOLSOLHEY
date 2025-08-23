package com.solsolhey.friend.repository;

import com.solsolhey.friend.entity.FriendInteraction;
import com.solsolhey.friend.entity.FriendInteraction.InteractionType;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 친구 상호작용 Repository
 */
@Repository
public interface FriendInteractionRepository extends JpaRepository<FriendInteraction, Long> {

    /**
     * 사용자가 받은 상호작용 목록 조회
     */
    @EntityGraph(attributePaths = {"fromUser"})
    @Query("SELECT fi FROM FriendInteraction fi WHERE fi.toUser = :toUser ORDER BY fi.createdAt DESC")
    Page<FriendInteraction> findByToUserOrderByCreatedAtDesc(@Param("toUser") User toUser, Pageable pageable);

    /**
     * 읽지 않은 상호작용 개수 조회
     */
    @Query("SELECT COUNT(fi) FROM FriendInteraction fi WHERE fi.toUser = :toUser AND fi.isRead = false")
    Long countUnreadByToUser(@Param("toUser") User toUser);

    /**
     * 특정 기간 동안의 상호작용 조회
     */
    @EntityGraph(attributePaths = {"fromUser", "toUser"})
    @Query("SELECT fi FROM FriendInteraction fi WHERE fi.createdAt BETWEEN :startDate AND :endDate")
    List<FriendInteraction> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 유형의 상호작용 조회
     */
    @EntityGraph(attributePaths = {"fromUser"})
    @Query("SELECT fi FROM FriendInteraction fi WHERE fi.toUser = :toUser AND fi.interactionType = :type " +
           "ORDER BY fi.createdAt DESC")
    List<FriendInteraction> findByToUserAndInteractionType(@Param("toUser") User toUser, 
                                                           @Param("type") InteractionType type);

    /**
     * 두 사용자 간의 최근 상호작용 조회
     */
    @Query("SELECT fi FROM FriendInteraction fi WHERE " +
           "((fi.fromUser = :user1 AND fi.toUser = :user2) OR " +
           "(fi.fromUser = :user2 AND fi.toUser = :user1)) " +
           "ORDER BY fi.createdAt DESC")
    List<FriendInteraction> findRecentInteractionsBetweenUsers(@Param("user1") User user1, 
                                                               @Param("user2") User user2, 
                                                               Pageable pageable);

    /**
     * 사용자의 오늘 받은 상호작용 수 조회
     */
    @Query("SELECT COUNT(fi) FROM FriendInteraction fi WHERE fi.toUser = :toUser AND " +
           "fi.createdAt >= :startOfDay AND fi.createdAt < :endOfDay")
    Long countTodayInteractionsByToUser(@Param("toUser") User toUser, 
                                       @Param("startOfDay") LocalDateTime startOfDay,
                                       @Param("endOfDay") LocalDateTime endOfDay);
}
