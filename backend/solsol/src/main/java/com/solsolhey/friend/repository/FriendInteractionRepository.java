package com.solsolhey.friend.repository;

import com.solsolhey.friend.entity.FriendInteraction;
import com.solsolhey.friend.entity.FriendInteraction.InteractionType;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
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
    @Query("SELECT fi FROM FriendInteraction fi WHERE fi.toUser = :toUser AND fi.isRead = false ORDER BY fi.createdAt DESC")
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

    /**
     * 대상 사용자에게 받은 특정 타입 상호작용 누적 카운트
     */
    long countByToUserAndInteractionType(User toUser, InteractionType type);

    /**
     * 주어진 기간 내 from -> to 방향, 특정 타입 상호작용 카운트 (예: LIKE 핑퐁 계산용)
     */
    @Query("SELECT COUNT(fi) FROM FriendInteraction fi WHERE fi.fromUser = :from AND fi.toUser = :to " +
           "AND fi.interactionType = :type AND fi.createdAt >= :start AND fi.createdAt < :end")
    long countDirectionalByTypeAndCreatedAtBetween(@Param("from") User from,
                                                   @Param("to") User to,
                                                   @Param("type") InteractionType type,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    /**
     * 주어진 기간 내 from -> to 방향, 특정 타입 상호작용의 가장 최근 생성 시각 (없으면 null)
     */
    @Query("SELECT MAX(fi.createdAt) FROM FriendInteraction fi WHERE fi.fromUser = :from AND fi.toUser = :to " +
           "AND fi.interactionType = :type AND fi.createdAt >= :start AND fi.createdAt < :end")
    LocalDateTime findMaxCreatedAtDirectionalByTypeAndCreatedAtBetween(@Param("from") User from,
                                                                       @Param("to") User to,
                                                                       @Param("type") InteractionType type,
                                                                       @Param("start") LocalDateTime start,
                                                                       @Param("end") LocalDateTime end);

    /**
     * 기간 제한 없이 방향별 최근 상호작용 시각
     */
    @Query("SELECT MAX(fi.createdAt) FROM FriendInteraction fi WHERE fi.fromUser = :from AND fi.toUser = :to AND fi.interactionType = :type")
    LocalDateTime findMaxCreatedAtDirectionalByType(@Param("from") User from,
                                                    @Param("to") User to,
                                                    @Param("type") InteractionType type);

    /**
     * 특정 시각 이후의 방향별 상호작용 수 (핑퐁 응답 검증용)
     */
    @Query("SELECT COUNT(fi) FROM FriendInteraction fi WHERE fi.fromUser = :from AND fi.toUser = :to AND fi.interactionType = :type AND fi.createdAt > :start")
    long countDirectionalByTypeAndCreatedAtAfter(@Param("from") User from,
                                                 @Param("to") User to,
                                                 @Param("type") InteractionType type,
                                                 @Param("start") LocalDateTime start);

    /**
     * 현재 사용자에게 온 모든 상호작용을 일괄 읽음 처리
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FriendInteraction fi SET fi.isRead = true WHERE fi.toUser = :toUser AND fi.isRead = false")
    int markAllAsReadByToUser(@Param("toUser") User toUser);

    /**
     * 참조 ID(예: 친구요청 friendId)로, 수신자 기준 특정 타입 알림 조회 (최신 우선)
     */
    @Query("SELECT fi FROM FriendInteraction fi WHERE fi.toUser = :toUser AND fi.interactionType = :type " +
           "AND fi.referenceId = :referenceId ORDER BY fi.createdAt DESC")
    List<FriendInteraction> findByToUserAndTypeAndReferenceId(@Param("toUser") User toUser,
                                                              @Param("type") InteractionType type,
                                                              @Param("referenceId") Long referenceId);
}
