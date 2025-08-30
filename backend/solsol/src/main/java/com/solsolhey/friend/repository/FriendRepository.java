package com.solsolhey.friend.repository;

import com.solsolhey.friend.entity.Friend;
import com.solsolhey.friend.entity.Friend.FriendshipStatus;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 Repository
 */
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * 두 사용자 간의 친구 관계 조회
     */
    @EntityGraph(attributePaths = {"user", "friendUser"})
    @Query("SELECT f FROM Friend f WHERE " +
           "(f.user = :user AND f.friendUser = :friend) OR " +
           "(f.user = :friend AND f.friendUser = :user)")
    Optional<Friend> findFriendshipBetween(@Param("user") User user, @Param("friend") User friend);

    /**
     * 두 사용자 간의 모든 친구 관계(양방향) 조회
     */
    @EntityGraph(attributePaths = {"user", "friendUser"})
    @Query("SELECT f FROM Friend f WHERE (f.user = :user AND f.friendUser = :friend) OR (f.user = :friend AND f.friendUser = :user)")
    List<Friend> findAllFriendshipsBetween(@Param("user") User user, @Param("friend") User friend);

    /**
     * 사용자의 친구 목록 조회 (상태별)
     */
    @EntityGraph(attributePaths = {"friendUser"})
    @Query("SELECT f FROM Friend f WHERE f.user = :user AND f.status = :status")
    Page<Friend> findByUserAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status, Pageable pageable);

    /**
     * 사용자가 받은 친구 요청 목록
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT f FROM Friend f WHERE f.friendUser = :user AND f.status = :status")
    Page<Friend> findByFriendUserAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status, Pageable pageable);

    /**
     * 사용자의 모든 수락된 친구 관계 조회 (양방향)
     */
    @Query("SELECT f FROM Friend f WHERE (f.user = :user OR f.friendUser = :user) AND f.status = :status")
    Page<Friend> findAcceptedFriends(@Param("user") User user, @Param("status") FriendshipStatus status, Pageable pageable);

    /**
     * 사용자의 모든 친구 수 조회
     */
    @Query("SELECT COUNT(f) FROM Friend f WHERE " +
           "(f.user = :user OR f.friendUser = :user) AND f.status = 'ACCEPTED'")
    Long countFriendsByUser(@Param("user") User user);

    /**
     * 상호 친구 여부 확인
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE " +
           "((f.user = :user1 AND f.friendUser = :user2) OR " +
           "(f.user = :user2 AND f.friendUser = :user1)) AND " +
           "f.status = 'ACCEPTED'")
    boolean existsMutualFriendship(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 닉네임으로 친구 검색 (현재 사용자 제외)
     */
    @EntityGraph(attributePaths = {"friendUser"})
    @Query("SELECT f FROM Friend f WHERE f.user = :user AND f.status = 'ACCEPTED' AND " +
           "LOWER(f.friendUser.nickname) LIKE LOWER(CONCAT('%', :nickname, '%'))")
    List<Friend> findFriendsByNickname(@Param("user") User user, @Param("nickname") String nickname);

    /**
     * 특정 사용자와의 친구 관계를 상태별로 조회
     */
    @Query("SELECT f FROM Friend f WHERE f.user = :user AND f.friendUser = :friendUser AND f.status = :status")
    Optional<Friend> findByUserAndFriendUserAndStatus(@Param("user") User user, @Param("friendUser") User friendUser, @Param("status") FriendshipStatus status);
}
